# ANX Network Bridge для BungeeCord

[English](README.md) · Русский

Официальный агент [anx.run](https://anx.run) для BungeeCord. Плагин передаёт агрегированные показатели доступности и
онлайна, показывает временные коды проверки в MOTD, учитывает игроков Floodgate и может выдавать награды за голосование.

## Требования и установка

- BungeeCord 1.21 или Waterfall; Java 21.
- Скачайте JAR и `.sha256` из [Releases](https://github.com/CtrlAltBogdan/anx-network-bridge-bungee/releases).
- Проверьте файл: `sha256sum -c anx-network-bridge-bungee-1.0.0.jar.sha256`.
- Поместите JAR в `plugins/`, перезапустите прокси и выполните в консоли `anx pair <код>`.
- Проверьте созданный `config.yml`, особенно соответствие режимов anx.run именам backend-серверов BungeeCord.

Ключ хранится в `plugins/ANXNetworkBridge/agent.key`; не публикуйте его. Телеметрия не содержит имён, UUID, IP-адресов
или чата игроков. Никнейм приходит отдельно только для настроенной владельцем выдачи награды.

Команда `anx status` показывает состояние API, очереди и проверки MOTD. Разрешение команд — `anx.admin`. При недоступности
API плагин сохраняет до 10 измерений не старше четырёх минут и повторно отправляет их по порядку.

## Сборка

```sh
./gradlew clean build
```

Сборка на Java 21 проверяет закреплённый Core, запускает интеграционный self-test и инспектирует итоговый JAR. Версия Core
записана в `core.lock`; обновляйте её только командой `scripts/update-core.sh vX.Y.Z` и коммитьте `core/` вместе с lock-файлом.

Лицензия MIT: [LICENSE](LICENSE). Уязвимости сообщайте по инструкции [SECURITY.md](SECURITY.md).
