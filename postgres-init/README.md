# PostgreSQL initialization scripts

Place `.sql`, `.sql.gz`, or executable `.sh` initialization scripts here. The official
PostgreSQL image runs these files alphabetically **only when the database data directory
is empty / the named volume is first initialized**.

Do not put secrets in these scripts. Do not put schema migrations here if you expect them
to run on every deployment; use Flyway/Liquibase or an explicit migration process instead.

The supplied `docker-compose.yml` mounts this directory at
`/docker-entrypoint-initdb.d:ro`. Existing `restaurant_postgres_data` volumes are not
reinitialized. Back up the database before removing a volume.
