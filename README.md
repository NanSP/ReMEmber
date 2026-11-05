# ReMEmber

Aplicativo Android de gerenciamento de objetivos e lembretes com gamificação. O ReMEmber combina organização pessoal, evolução visual de cenários e personalização de avatar para incentivar a criação de hábitos e a conclusão de atividades.

O projeto foi desenvolvido em Kotlin com Jetpack Compose e utiliza uma interface inspirada em neomorfismo. Todos os dados funcionais são mantidos localmente no aparelho.

## Funcionalidades

### Objetivos e lembretes

- Criação, edição, conclusão e exclusão de objetivos.
- Categorias de estudo, exercício, alimentação e tarefas gerais.
- Definição de uma categoria principal para orientar as recompensas e o cenário.
- Recorrência diária, semanal ou sem recorrência.
- Configuração de horário para lembretes locais.
- Reagendamento dos alarmes após reinicialização, atualização do aplicativo e alterações de horário ou fuso do aparelho.
- Histórico de conclusões com filtros por categoria e período.
- Bloqueio temporário para troca da categoria principal.

### Gamificação

- Experiência, níveis e moedas virtuais.
- Sequência diária baseada no dia e horário do aparelho.
- Recompensas adicionais para a categoria principal.
- Missões bônus diárias.
- Conquistas associadas ao progresso do usuário.
- Recuperação limitada de sequência.
- Recompensas sazonais para sequências prolongadas.

### Avatar e loja

- Avatar modular montado por camadas SVG.
- Três formatos de corpo com peças adaptadas ao formato selecionado.
- Personalização de rosto, olhos, expressão, cabelo, roupas e acessórios.
- Alteração de cores de pele, cabelo e olhos.
- Catálogo de itens desbloqueados e equipados.
- Loja com conjuntos temáticos, incluindo os conjuntos Punk e Bruxa.
- Compra com moedas virtuais e persistência dos itens adquiridos.

### Cenários

- Evolução visual conforme o progresso do usuário.
- Cenários associados às categorias, como biblioteca, academia, horta e organização do quarto.
- Composição em camadas para posicionar corretamente os elementos visuais.

### Interface e experiência

- Primeiro acesso com criação de perfil, categoria principal e avatar inicial.
- Tela de abertura animada.
- Tela inicial com sequência, moedas, nível, objetivos e notificações.
- Componentes reutilizáveis com estilo neomórfico.
- Configurações de som, vibração e lembretes.
- Navegação entre Início, Objetivos, Avatar, Loja, Histórico, Perfil e Configurações.

## Tecnologias

- **Kotlin 2.2.10** — linguagem principal.
- **Jetpack Compose + Material 3** — construção da interface declarativa.
- **Room** — armazenamento de objetivos e registros de conclusão.
- **DataStore Preferences** — perfil, progresso, preferências, inventário e demais estados simples.
- **AlarmManager e BroadcastReceiver** — lembretes locais e reagendamento de alarmes.
- **Coil 3** — carregamento e composição dos assets SVG.
- **KSP** — geração de código do Room.
- **JUnit, AndroidX Test e Espresso** — testes unitários, de integração e instrumentados.
- **Gradle Kotlin DSL** — configuração do projeto e dependências.
- **GitHub Actions** — lint, testes e geração do APK em pull requests.

## Arquitetura e organização

O projeto possui um único módulo Android, `app`, organizado internamente por funcionalidade. Essa estrutura mantém cada domínio próximo das suas regras, dados e telas, sem separar artificialmente o aplicativo em pastas de “frontend” e “backend”. Como o ReMEmber funciona localmente, a interface e a camada de dados fazem parte do mesmo aplicativo.

```text
ReMEmber/
├── .github/
│   └── workflows/                 # Pipeline de pull requests
├── app/
│   ├── schemas/                   # Histórico de schemas do Room
│   └── src/
│       ├── main/
│       │   ├── java/com/example/remember/
│       │   │   ├── app/           # Composição da aplicação e fluxo principal
│       │   │   ├── core/          # Recursos compartilhados
│       │   │   ├── feature/       # Funcionalidades organizadas por domínio
│       │   │   ├── ui/            # Tema e componentes visuais reutilizáveis
│       │   │   └── MainActivity.kt
│       │   ├── res/               # Strings, fontes, ícones e recursos Android
│       │   └── AndroidManifest.xml
│       ├── test/                   # Testes unitários executados na JVM
│       └── androidTest/            # Testes executados em aparelho/emulador
├── design/
│   └── assets/                    # SVGs do avatar e dos cenários
├── docs/                          # Requisitos e documentação complementar
├── gradle/
│   └── libs.versions.toml         # Catálogo central de versões
├── build.gradle.kts               # Configuração Gradle da raiz
└── settings.gradle.kts            # Declaração dos módulos
```

### Pacote `app`

Contém `RememberMeApp`, responsável por observar o estado global, controlar o primeiro acesso e conectar a navegação às operações do repositório.

### Pacote `core`

Infraestrutura compartilhada entre funcionalidades:

```text
core/
├── assets/                        # Resolução dos caminhos de assets
├── navigation/                    # Destinos de navegação
└── notifications/                 # Contrato de agendamento de lembretes
```

### Pacote `feature`

Cada subpasta representa uma área funcional:

| Pacote | Responsabilidade |
|---|---|
| `avatar` | Catálogo, configuração e renderização em camadas do avatar. |
| `gamification` | Modelos de experiência, moedas, nível e sequência. |
| `goals` | Categorias, modelos e contratos de objetivos. |
| `history` | Modelos dos registros de conclusão. |
| `mvp` | Implementação integrada atual: repositório, Room, regras e telas. |
| `onboarding` | Estado e modelos do primeiro acesso. |
| `profile` | Dados locais do perfil do usuário. |
| `scenes` | Catálogo, camadas e evolução dos cenários. |
| `settings` | Preferências gerais do aplicativo. |
| `store` | Catálogo, conjuntos e regras de compra da loja. |
| `today` | Componentes relacionados à visão diária. |

As funcionalidades mais completas seguem uma separação em camadas:

```text
feature/<funcionalidade>/
├── data/                           # Persistência, catálogos e integrações Android
├── domain/                         # Modelos e regras de negócio puras
└── presentation/                   # Telas e componentes Compose
```

### Pacote `feature/mvp`

O pacote `mvp` concentra a implementação funcional atualmente conectada às telas:

```text
feature/mvp/
├── data/
│   ├── local/                     # Entidades, DAOs, mapeadores e banco Room
│   ├── MvpRepository.kt           # Coordenação do estado e das operações
│   ├── GoalReminderScheduler.kt   # Agendamento dos lembretes
│   ├── GoalReminderReceiver.kt    # Recepção e exibição das notificações
│   └── *Codec.kt                  # Compatibilidade e serialização de estados
├── domain/
│   ├── MvpState.kt                # Estado agregado do aplicativo
│   ├── RewardRules.kt             # XP, moedas, sequência e temporadas
│   ├── GamificationRules.kt       # Missões, conquistas e recuperação
│   └── HistoryFilters.kt          # Filtros do histórico
└── presentation/
    ├── NeoHomeScreen.kt           # Tela inicial neomórfica
    ├── MvpScreens.kt              # Objetivos, avatar, loja, perfil e ajustes
    ├── HistoryScreen.kt           # Histórico de conclusões
    └── BrandLoadingScreen.kt      # Tela inicial de carregamento
```

## Persistência dos dados

O armazenamento local é dividido de acordo com a natureza dos dados:

- **Room:** objetivos e conclusões, pois são registros estruturados, consultáveis e relacionados ao histórico.
- **DataStore:** perfil, XP, moedas, sequência, preferências, avatar, inventário, conquistas e estados auxiliares.
- **AlarmManager:** mantém o próximo disparo de cada lembrete no sistema Android.

O repositório coordena essas fontes e expõe um estado único para a interface Compose. Há também uma rotina de recuperação para evitar que uma conclusão seja recompensada mais de uma vez caso uma operação seja interrompida entre o Room e o DataStore.

## Assets SVG

Os arquivos de avatar e cenário ficam em `design/assets` e são incluídos no aplicativo como Android Assets pelo Gradle.

```text
design/assets/
├── avatar/                         # Biblioteca modular atual do avatar
├── avatar_studio/                  # Fontes visuais adicionais do avatar
└── cenarios/                       # Elementos de evolução dos cenários
```

Os SVGs do avatar usam o mesmo canvas e são renderizados em uma ordem definida pelo `AvatarRenderer`. Ao adicionar uma peça, é importante manter escala e posicionamento compatíveis com as demais camadas e cadastrar o item no `AvatarCatalog`.

## Requisitos para desenvolvimento

- Android Studio ou IntelliJ IDEA com suporte Android.
- Android SDK instalado.
- JDK compatível com a versão do Gradle do projeto.
- Dispositivo com Android 8.0 (API 26) ou superior, ou emulador Android.
- Depuração USB habilitada para testes em aparelho físico.

O projeto utiliza `compileSdk 37`, `targetSdk 37` e `minSdk 26`.

## Como executar

1. Clone o repositório:

   ```bash
   git clone <URL_DO_REPOSITORIO>
   cd ReMEmber
   ```

2. Abra a pasta raiz no Android Studio ou IntelliJ IDEA.
3. Aguarde a sincronização do Gradle.
4. Conecte um aparelho autorizado ou inicie um emulador.
5. Execute a configuração `app` pela IDE.

Também é possível gerar o APK pelo terminal.

No Windows:

```powershell
.\gradlew.bat :app:assembleDebug
```

No Linux ou macOS:

```bash
./gradlew :app:assembleDebug
```

O APK será gerado em:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Testes e qualidade

Executar testes unitários:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Executar lint e gerar o APK debug:

```powershell
.\gradlew.bat :app:lintDebug :app:assembleDebug
```

Executar testes instrumentados com um aparelho ou emulador conectado:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

Os testes cobrem, entre outros pontos:

- Regras de recompensas e sequências.
- Recorrência e serialização de objetivos.
- Filtros de histórico.
- Catálogos de avatar, loja e cenários.
- Persistência de objetivos e conclusões no Room.
- Fluxo integrado de criação, edição e conclusão.
- Recuperação de operações interrompidas.
- Agendamento e cancelamento de lembretes.

## Integração contínua

Pull requests acionam o workflow `.github/workflows/pull-request.yml`, composto por dois jobs:

1. Lint, testes unitários e build debug.
2. Testes instrumentados em um emulador Android API 35.

## Permissões Android

O aplicativo declara as seguintes permissões:

- `POST_NOTIFICATIONS` — apresentação dos lembretes nas versões recentes do Android.
- `SCHEDULE_EXACT_ALARM` — disparo dos lembretes no horário configurado.
- `RECEIVE_BOOT_COMPLETED` — restauração dos alarmes após reiniciar o aparelho.

## Estado do projeto

O ReMEmber está em desenvolvimento e funciona como um aplicativo local. Não existe atualmente um servidor ou API externa; uma futura sincronização em nuvem poderá ser incorporada como uma nova camada de dados sem alterar a organização principal das funcionalidades.
