# MVP para teste em aparelho

## Fluxos disponíveis

- Criar perfil local com nome e categoria principal.
- Navegar entre Início, Objetivos, Avatar e Mais.
- Criar, concluir e excluir objetivos.
- Receber XP e moedas ao concluir um objetivo.
- Receber recompensa maior para a categoria principal.
- Atualizar sequência diária e maior sequência.
- Personalizar cabelo, roupa e óculos do avatar.
- Consultar perfil, prévia da loja e configurações.
- Manter dados no aparelho por meio do DataStore.

## Roteiro de teste

1. Abra o aplicativo e crie um perfil.
2. Selecione uma categoria principal.
3. Crie dois objetivos, um da categoria principal e outro de uma categoria diferente.
4. Conclua os dois e compare as recompensas apresentadas.
5. Feche completamente o aplicativo e abra novamente.
6. Confirme que perfil, objetivos, XP, moedas e avatar foram preservados.
7. Altere cabelo, roupa, óculos, som e vibração.
8. Visite Perfil, Loja e Configurações pelo menu Mais.
9. Exclua um objetivo e confirme sua remoção.

## Instalação pelo terminal

Com a depuração USB ativa e o aparelho autorizado:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r app\build\outputs\apk\debug\app-debug.apk
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" shell am start -n com.example.remember/.MainActivity
```

## Fora deste protótipo

Room, notificações, WorkManager, missões bônus, conquistas, compra de itens,
multiplicadores, recuperação de sequência e evolução completa dos cenários permanecem
para as próximas iterações. A Loja atual é apenas uma prévia e não desconta moedas.
