Si trabajan desde ...

1. WSL, Git Bash, Mac o Linux -> ./start-all.sh   (detener: ./stop-all.sh, o ./stop-all.sh --db para bajar también la base)
2. PowerShell puro en Windows -> .\start-all.ps1  (detener: .\stop-all.ps1, o .\stop-all.ps1 -Db)

Antes de la primera vez, crear estos archivos (no se suben a git; pide los valores al equipo):

- helpdeskpro-db/.env                (copiar de .env.example)
- helpdeskpro-backend/.env           COGNITO_JWK_SET_URI, COGNITO_ISSUER_URI, COGNITO_CLIENT_ID
- helpdeskpro-frontend/.env.local    VITE_API_URL, VITE_COGNITO_AUTHORITY, VITE_COGNITO_CLIENT_ID,
                                     VITE_COGNITO_REDIRECT_URI, VITE_COGNITO_DOMAIN, VITE_COGNITO_LOGOUT_URI

Los scripts revisan que todo esté antes de arrancar y te dicen qué falta.
