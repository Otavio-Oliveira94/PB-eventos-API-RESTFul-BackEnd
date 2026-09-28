# Arquitetura Orientada a Eventos com RabbitMQ

## 1. Contexto

O sistema Eventos Express era inicialmente baseado em comunicação síncrona.

O `inscricoes-service` utilizava OpenFeign para consultar diretamente o
`eventos-service` antes de criar ou alterar uma inscrição.

Essa abordagem criava acoplamento temporal: para concluir uma inscrição,
o serviço de eventos precisava estar disponível e responder imediatamente.

O sistema foi refatorado para utilizar comunicação assíncrona baseada em
eventos por meio do RabbitMQ.

## 2. Arquitetura anterior

O fluxo anterior era:

1. O cliente solicitava a criação de uma inscrição.
2. O `inscricoes-service` consultava o `eventos-service` por HTTP usando Feign.
3. O `eventos-service` verificava a existência do evento.
4. A inscrição era criada somente depois da resposta HTTP.

### Problemas identificados

- Dependência direta entre os dois serviços.
- Falha na inscrição quando o serviço de eventos estava indisponível.
- Maior tempo de resposta para o cliente.
- Dificuldade para adicionar novos consumidores.
- Necessidade de conhecer o endereço HTTP do outro serviço.
- Menor tolerância a falhas temporárias.

## 3. Arquitetura atual

A comunicação entre os backends passou a utilizar RabbitMQ.

O `inscricoes-service` registra inicialmente a inscrição com o estado
`PENDENTE` e publica uma solicitação de validação.

O `eventos-service` consome a solicitação, verifica a existência do evento
e publica o resultado.

O `inscricoes-service` consome o resultado e altera a inscrição para:

- `CONFIRMADA`, quando o evento existe;
- `REJEITADA`, quando o evento não existe.

A API responde com HTTP 202 Accepted, indicando que a solicitação foi aceita
e continuará sendo processada de maneira assíncrona.

## 4. Fluxo de criação da inscrição

1. O frontend envia uma requisição ao `inscricoes-service`.
2. A inscrição é persistida com estado `PENDENTE`.
3. Após o commit da transação, uma mensagem é publicada no RabbitMQ.
4. O `eventos-service` recebe a solicitação.
5. O evento informado é validado.
6. O resultado da validação é publicado.
7. O `inscricoes-service` recebe o resultado.
8. A inscrição é confirmada ou rejeitada.
9. O frontend consulta novamente a inscrição para visualizar o estado final.

## 5. Vantagens da arquitetura orientada a eventos

### Menor acoplamento

O `inscricoes-service` não precisa mais conhecer o endereço HTTP do
`eventos-service`. A integração ocorre por meio de contratos de mensagens.

### Resiliência

As mensagens permanecem nas filas quando um consumidor está temporariamente
indisponível e podem ser processadas quando ele voltar a funcionar.

### Escalabilidade

É possível executar várias instâncias de um consumidor. O RabbitMQ distribui
as mensagens entre elas por meio do padrão Work Queue.

### Extensibilidade

Um mesmo evento pode ser entregue a diferentes filas. Isso permite adicionar
novos consumidores sem alterar o produtor da mensagem.

### Processamento assíncrono

O cliente não precisa aguardar todo o processamento interno. A API aceita a
solicitação e o processamento continua em segundo plano.

### Rastreabilidade

Cada fluxo utiliza identificadores como `mensagemId` e `solicitacaoId`,
permitindo correlacionar as mensagens envolvidas em uma operação.

## 6. Desvantagens e desafios

### Consistência eventual

A inscrição permanece temporariamente no estado `PENDENTE`. O resultado da
operação não está disponível imediatamente após a requisição.

### Maior complexidade operacional

O RabbitMQ passa a ser mais um componente que precisa ser configurado,
monitorado e mantido.

### Possibilidade de mensagens duplicadas

Uma mensagem pode ser entregue novamente em determinadas situações. Por isso,
o consumidor deve verificar o estado atual antes de aplicar uma alteração.

### Depuração distribuída

Uma operação passa por diferentes serviços e filas, tornando a investigação
de problemas mais complexa do que em uma chamada HTTP direta.

## 7. Quando essa arquitetura é vantajosa

A arquitetura orientada a eventos é indicada quando:

- o processamento não precisa terminar imediatamente;
- vários componentes precisam reagir ao mesmo acontecimento;
- os serviços precisam funcionar com menor dependência temporal;
- é necessário absorver picos de requisições;
- os consumidores precisam ser escalados independentemente;
- falhas temporárias não devem causar a perda imediata da operação.

Ela pode não ser a melhor escolha quando:

- o resultado precisa ser imediato e fortemente consistente;
- a aplicação é pequena e possui poucos componentes;
- a complexidade de operar um message broker não é justificável;
- uma chamada HTTP simples já atende completamente ao caso de uso.

## 8. Decisão arquitetural

Para este projeto, a arquitetura orientada a eventos foi escolhida porque a
validação de uma inscrição pode acontecer de forma assíncrona.

A utilização do RabbitMQ reduziu o acoplamento entre os serviços e permitiu
implementar filas duráveis, confirmação de publicação, novas tentativas,
Dead Letter Queue, Pub/Sub e Work Queue.

O custo dessa decisão é a adoção de consistência eventual e o aumento da
complexidade do fluxo. Para tornar esse comportamento visível, a inscrição
possui estados explícitos e o frontend permite consultar o resultado do
processamento.