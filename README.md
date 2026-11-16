# Serviço de Assinatura de Marmitas (Minha Refeição)

Projeto da disciplina **Projeto de Software** — Universidade Presbiteriana Mackenzie — Profa. Ana Claudia Rossi.
Este documento apresenta a estruturação das entregas avaliativas do projeto.

**Caso de uso:** Assinar Plano de Refeições · **Ator principal:** Assinante · **Ator secundário:** Operadora de Cartão de Crédito

---

## GRUPO

| Nome | RA |
| ------------------------ | -------- |
| Gabriel Valieri dos Santos | 10736421 |
| Enzo Bueno Nichimura | 10737959 |

---

## N1

### N1 - Entrega 1 — Protótipo (Sequência de Telas) e Lista de Classes Candidatas

- **Descrição:** protótipo de baixa fidelidade com a sequência de telas do caminho feliz do cenário "Assinar Plano de Refeições" e levantamento das classes candidatas.
- **Protótipo (Figma):** https://www.figma.com/make/oHlOanAmn6In1dbohiY7C5/Minha-Refei%C3%A7%C3%A3o---App-de-Marmitas?t=ONGFybpxDoXELOW5-20&fullscreen=1
- **Vídeo explicativo:** https://youtu.be/PXIiA1WybWY

### N1 - Entrega 2 — Diagrama de Classes de Domínio

- **Descrição:** modelo conceitual do caso de uso — classes, atributos, associações, composição, agregação, classe associativa e generalização.
- **Wiki:** [Diagrama de Classes de Domínio](../../wiki/N1%E2%80%90Entrega%E2%80%902%E2%80%90Diagrama%E2%80%90de%E2%80%90Classes%E2%80%90de%E2%80%90Dominio)

---

## N2

### N2 - Entrega 3 — Diagramas de Sequência e Diagrama de Classes de Projeto

- **Descrição:** cinco diagramas de sequência (DS1 a DS5) cobrindo os passos 1 a 31 do cenário e os fluxos alternativos FA1, FA2 e FA3, mais o diagrama de classes de projeto derivado deles.
- **Wiki:** [Diagramas de Sequência](../../wiki/N2%E2%80%90Entrega%E2%80%903%E2%80%90Diagramas%E2%80%90de%E2%80%90Sequencia) · [Diagrama de Classes de Projeto](../../wiki/N2%E2%80%90Entrega%E2%80%903%E2%80%90Diagrama%E2%80%90de%E2%80%90Classes%E2%80%90de%E2%80%90Projeto) · [Checklist de Autoavaliação](../../wiki/Checklist%E2%80%90de%E2%80%90Autoavaliacao)
- **Fontes dos diagramas:** [`docs/diagramas/`](docs/diagramas)

### N2 - Entrega 4 — Implementação do Cenário do Caso de Uso

- **Descrição:** tradução dos modelos de projeto para código Java, mantendo a correspondência entre os diagramas de sequência e a implementação. Estão implementados os objetos **fronteira** (telas de console), **controle** (controlador do caso de uso) e **entidade/domínio**, com **persistência em arquivos de texto**.
- **Wiki da entrega:** [Implementação do Cenário do Caso de Uso](../../wiki/N2%E2%80%90Entrega%E2%80%904%E2%80%90Implementacao%E2%80%90do%E2%80%90Cenario)
- **Código-fonte:** [`src/`](src) · **Evidências de execução:** [na Wiki da entrega](../../wiki/N2%E2%80%90Entrega%E2%80%904%E2%80%90Implementacao%E2%80%90do%E2%80%90Cenario#6-evidências-de-execução)
- **Vídeo da Entrega 4:** https://youtu.be/U07aJ_anCTo

---

## Implementação (N2 - Entrega 4)

### Requisitos

- JDK **17 ou superior** (`java -version` e `javac -version` devem funcionar no terminal)
- Maven 3.8+ é **opcional** — o projeto não possui dependências externas

### Compilar e executar (somente JDK)

Linux / macOS:

```bash
javac -encoding UTF-8 -d build/classes $(find src/main -name "*.java")
java -cp build/classes br.mackenzie.minharefeicao.App                 # menu interativo
java -cp build/classes br.mackenzie.minharefeicao.App --demo          # demonstração automática
java -cp build/classes br.mackenzie.minharefeicao.App --dados ./dados # escolhe a pasta de dados
```

Windows (PowerShell):

```powershell
chcp 65001
javac -encoding UTF-8 -d build\classes (Get-ChildItem -Recurse -Filter *.java src\main).FullName
java -Dstdout.encoding=UTF-8 -cp build\classes br.mackenzie.minharefeicao.App
```

### Gerar e executar o JAR

```bash
javac -encoding UTF-8 -d build/classes $(find src/main -name "*.java")
jar cfe dist/minha-refeicao.jar br.mackenzie.minharefeicao.App -C build/classes .
java -jar dist/minha-refeicao.jar
```

Com Maven (opcional): `mvn clean package` e `java -jar target/minha-refeicao-1.0.0.jar`.

### Executar os testes automatizados

```bash
javac -encoding UTF-8 -d build/classes $(find src -name "*.java")
java -cp build/classes br.mackenzie.minharefeicao.TesteRegrasNegocio   # 9 testes das regras do cenário
java -cp build/classes br.mackenzie.minharefeicao.TestePersistencia    # 5 testes da persistência
```

### Menu da aplicação

| Opção | Operação |
|---|---|
| 1 | Assinar plano de refeições (passos 1 a 31 do cenário) |
| 2 | Consultar assinaturas gravadas (lidas dos arquivos de dados) |
| 3 | Demonstração automática do fluxo principal e dos fluxos alternativos |
| 0 | Sair |

### Como exercitar cada fluxo

| Fluxo | Como provocar |
|---|---|
| **Principal (1–31)** | Siga as telas. O código de confirmação aparece no console como `[SMS simulado]`. Use um cartão que **não** termine em `0000`, por exemplo `4111111111111111`, validade `12/30`, CVV `123`. |
| **FA1 — código inválido** | Digite um código errado. O sistema informa quantas tentativas restam e, na terceira, bloqueia o código e oferece o envio de um novo. |
| **FA2 — limite do plano** | No plano *Essencial* (5 refeições), peça 9 unidades de um prato principal. O sistema recusa e informa o limite e o saldo. |
| **FA3 — pagamento não autorizado** | Use um cartão terminado em `0000` (ex.: `5555444433330000`). Responda `s` para tentar outro cartão ou `n` para encerrar sem ativação (FA3.6). |

Dados de exemplo: celular `11987654321`; CEP `01302-907`.

### Persistência dos dados

Os dados são gravados em **arquivos de texto UTF-8** na pasta `dados/` (configurável com `--dados`), com um registro por linha e campos separados por `|`:

| Arquivo | Conteúdo |
|---|---|
| `assinantes.txt` | celular, confirmação do celular, preferências alimentares e endereço de entrega |
| `assinaturas.txt` | código, celular, plano, data, status da assinatura e do pedido, protocolo e previsão da 1ª entrega |
| `itens_pedido.txt` | itens do pedido de cada assinatura (código do item e quantidade) |
| `pagamentos.txt` | tentativas de pagamento: data, valor, cartão **mascarado**, status e código de autorização |

Os planos e o cardápio são pré-cadastrados em `DadosIniciais` e carregados em memória na inicialização. Ao iniciar, os repositórios leem os arquivos e **reconstroem os objetos de domínio**, de modo que a opção 2 do menu mostra assinaturas criadas em execuções anteriores. O número completo do cartão e o CVV nunca são gravados.

### Estrutura do projeto

```
servicoassinaturamarmitas-minharefeicao-GabrieleEnzo/
├── pom.xml
├── README.md
├── dados/                              # criada na execução (arquivos .txt de persistência)
├── docs/
│   └── diagramas/                      # fontes Mermaid (.mmd) e imagens (.png)
└── src/
    ├── main/java/br/mackenzie/minharefeicao/
    │   ├── App.java                    # ponto de entrada (injeção de dependências)
    │   ├── ui/                         # fronteira: MenuPrincipal, TelaAssinatura, TelaConsultaAssinaturas
    │   ├── controle/                   # AssinarPlanoController + DTO de confirmação
    │   ├── dominio/                    # entidades e regras de negócio
    │   ├── servico/                    # SMS, operadora de cartão e protocolo (interfaces + simulações)
    │   └── persistencia/               # repositórios, ArquivoTexto e carga inicial
    └── test/java/br/mackenzie/minharefeicao/
        ├── TesteRegrasNegocio.java     # fluxo principal e FA1, FA2, FA3
        └── TestePersistencia.java      # gravação e recarga dos objetos de domínio
```

### Regras de negócio implementadas

- O celular deve ter DDD + 9 dígitos; o código de confirmação tem 6 dígitos e admite **3 tentativas** (FA1).
- O cardápio exibe apenas itens **disponíveis** e compatíveis com as **preferências** registradas (Vegetariana e Sem lactose são restrições; Tradicional não restringe).
- Cada categoria (prato principal, acompanhamento e sobremesa) aceita no máximo a **quantidade de refeições do plano** (FA2).
- Status da assinatura: `Em elaboração → Aguardando Pagamento → Ativa` (ou `Encerrada sem ativação`, FA3.6); status do pedido: `Em elaboração → Aprovado` (ou `Cancelado`).
- Pagamento aprovado gera protocolo `MR-AAAAMMDD-NNNNNN` e previsão da primeira entrega (hoje + 3 dias).
- SMS e operadora de cartão são **simulados** atrás das interfaces `ServicoSMS` e `OperadoraCartao`, podendo ser trocados por integrações reais sem alterar o controlador.
