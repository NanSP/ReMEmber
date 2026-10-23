# Estrutura do projeto

O ReMEmber usa uma organização por funcionalidade dentro de um único módulo Android.

- `app`: ponto de entrada e composição geral da aplicação.
- `core`: infraestrutura compartilhada, como assets, navegação e notificações.
- `feature/avatar`: catálogo, configuração e renderização modular do avatar.
- `feature/goals`: modelos e contratos relacionados aos objetivos.
- `feature/gamification`: XP, moedas, sequências e recuperações.
- `feature/today`: tela inicial e objetivos do dia.
- `feature/scenes`: catálogo e progressão visual dos cenários.
- `feature/onboarding`: estado do primeiro acesso e permissões iniciais.
- `feature/history`: registros de conclusão dos objetivos.
- `feature/store`: itens cosméticos compráveis com moedas virtuais.
- `feature/profile`: dados do usuário e configuração equipada do avatar.
- `feature/settings`: preferências de lembretes, som, vibração e movimento.
- `design/assets`: fontes SVG editáveis e também assets empacotados no aplicativo.

## Assets

Os SVGs possuem canvas `1024 x 1024` e devem preservar posição e escala para que as
camadas continuem alinhadas. O Gradle inclui `design/assets` no source set principal,
portanto os arquivos ficam disponíveis por caminhos como:

```text
file:///android_asset/avatar/corpo/03_corpo_medio.svg
file:///android_asset/cenarios/biblioteca/biblioteca_estante_cheia.svg
```

A ordem base de renderização do avatar é:

```text
corpo
calça
calçado
roupa
rosto
olhos
expressão
detalhe facial
cabelo
brincos
óculos
cachecol
relógio
chapéu
item especial
```

O catálogo Kotlin deve ser a fonte de verdade para IDs, slots e caminhos utilizados
pelo aplicativo. Os manifestos JSON fornecidos com os assets são mantidos para auditoria.
