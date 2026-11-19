# Timer Zap

Um toque no ícone (re)inicia o timer. Quando o tempo acaba, toca um alarme com o botão **Parar e abrir WhatsApp Business**.

- **1 toque no ícone:** zera e começa a contar (padrão: 25 min)
- **Segurar o ícone → Configurar:** muda o tempo e o app que abre
- **Segurar o ícone → Atualizar:** baixa a versão mais nova daqui e abre o instalador
- **Alarme:** toca em loop até você tocar em "Parar e abrir…" ou "Só parar"

## Instalar
O APK é compilado pelo GitHub Actions a cada push e publicado em **Releases → latest → Timer25.apk**.

## Assinatura
`release.p12` é a chave de assinatura (protegida por senha). A senha fica só no Secret `KEYSTORE_PASSWORD`.
Sem a mesma chave o Android recusa atualizar por cima — não apague nem troque este arquivo.
