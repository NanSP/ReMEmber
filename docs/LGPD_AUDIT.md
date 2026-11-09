# Varredura de privacidade e LGPD — ReMEmber

Data da revisão técnica: 9 de novembro de 2026.

## Escopo verificado

- Manifesto e permissões Android.
- Persistência em Room e DataStore.
- Dados apresentados nas telas de perfil, objetivos, histórico, avatar, loja e configurações.
- Lembretes agendados com AlarmManager.
- Regras de backup e transferência entre aparelhos.
- Dependências declaradas no Gradle.
- Fluxos de acesso, correção, exportação e exclusão.

## Inventário de dados

| Categoria | Exemplos | Finalidade | Armazenamento |
|---|---|---|---|
| Identificação local | Nome do perfil | Personalizar a experiência | DataStore |
| Organização pessoal | Título, categoria, recorrência e horário de objetivos | Gerenciar objetivos e lembretes | Room |
| Histórico de uso | Datas e recompensas de conclusões | Histórico e estatísticas | Room |
| Gamificação | XP, moedas, sequência, missões e conquistas | Calcular e apresentar progresso | DataStore |
| Avatar e inventário | Peças, cores e conjuntos adquiridos | Personalização visual | DataStore |
| Preferências | Som e vibração | Configurar notificações | DataStore |
| Transparência | Versão e horário da ciência do aviso | Registrar a apresentação do aviso | DataStore |

Não foram encontrados SDKs de publicidade, analytics, autenticação, localização ou sincronização em nuvem. O aplicativo não declara a permissão `INTERNET`.

## Permissões

| Permissão | Uso identificado |
|---|---|
| `POST_NOTIFICATIONS` | Exibir lembretes configurados pelo usuário. |
| `SCHEDULE_EXACT_ALARM` | Disparar lembretes no horário escolhido. |
| `RECEIVE_BOOT_COMPLETED` | Reagendar lembretes após reinicialização. |

## Adequações implementadas

- Aviso resumido antes da coleta do nome no primeiro acesso.
- Registro local da versão e do horário de ciência do aviso.
- Tela `Mais → Privacidade e LGPD` com dados, finalidades, retenção, permissões e compartilhamento.
- Resumo dos dados atualmente armazenados.
- Correção do nome do perfil.
- Exportação voluntária dos dados em JSON pelo seletor seguro do Android.
- Exclusão integral de Room e DataStore, cancelamento de alarmes e remoção de notificações.
- Backup em nuvem e transferência automática entre aparelhos explicitamente desativados.
- Canal de contato apontando para as issues do repositório.

## Retenção e compartilhamento

Os dados permanecem no armazenamento privado do app até a exclusão solicitada pelo usuário ou a desinstalação. Não há compartilhamento automático. A exportação somente ocorre após ação explícita e escolha do aplicativo de destino pelo usuário.

## Pendências organizacionais antes de produção

Estas medidas técnicas não substituem governança e revisão jurídica. Antes de distribuir comercialmente o aplicativo, o responsável deve:

1. Informar nome ou razão social, documento e contato oficial do controlador.
2. Confirmar e documentar a hipótese legal aplicável a cada operação de tratamento.
3. Definir procedimento de atendimento a titulares e incidentes de segurança.
4. Manter registro das versões do aviso e das mudanças de finalidade.
5. Revisar o aviso sempre que forem adicionados servidor, login, analytics, publicidade ou terceiros.
6. Publicar externamente o aviso de privacidade na página da loja e/ou site oficial.
7. Avaliar público infantil/adolescente antes da distribuição e, se aplicável, implementar as salvaguardas específicas.

## Referências oficiais

- Lei nº 13.709/2018 — Lei Geral de Proteção de Dados Pessoais.
- Direitos dos titulares: https://www.gov.br/anpd/pt-br/assuntos/titular-de-dados-1/direito-dos-titulares
- Resolução CD/ANPD nº 2/2022: https://www.gov.br/anpd/pt-br/acesso-a-informacao/institucional/atos-normativos/regulamentacoes_anpd/resolucao-cd-anpd-no-2-de-27-de-janeiro-de-2022
- Guia de Segurança da Informação para Agentes de Tratamento de Pequeno Porte: https://www.gov.br/anpd/pt-br/centrais-de-conteudo/materiais-educativos-e-publicacoes/anonimizado___guia_orientat-_seg_da_inf_p_atpp.pdf
