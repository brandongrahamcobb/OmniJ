export BASHRC_MODE=${BASHRC_MODE:-default}
if [[ "$BASHRC_MODE" == "Lucy" ]]; then
    source ~/.bashrc_lucy
elif [[ "$BASHRC_MODE" == "OmniJ" ]]; then
    source ~/.bashrc_omnij
elif [[ "$BASHRC_MODE" == "Disco" ]]; then
    source ~/.bashrc_disco
elif [[ "$BASHRC_MODE" == "OmniPy" ]]; then
    source ~/.bashrc_pyv
elif [[ "$BASHRC_MODE" == "Vyrtuous" ]]; then
    source ~/.bashrc_vyrtuous
elif [[ "$BASHRC_MODE" == "llama" ]]; then
    source ~/.bashrc_llama
fi
export CLI_MODEL="yur-cli-model"
export CLI_PROVIDER="your-cli-server-provider"
export CLI_REQUEST_TYPE="your-cli-request-type"
export DISCORD_API_KEY="your-discord-api-key"
export DISCORD_COMMAND_PREFIX="!"
export DISCORD_CONTEXT_LENGTH=2000
export DISCORD_MODEL="your-discord-model-string"
export DISCORD_OWNER_ID="your-discord-long"
export DISCORD_PROVIDER="llama"
export DISCORD_REQUEST_TYPE="deprecated"
export DISCORD_ROLE_PASS="your=discord-role-pass"
export DISCORD_STORE=false
export DISCORD_STREAM=true
export DISCORD_TESTING_GUILD_ID="your-discord-guild-id-long"
export GEMINI_API_KEY="your-gemini-api-key"
export GOOGLE_API_KEY="your-google-api-key"
export GOOGLE_CSE_ID="your-google-cse-id"
export LOGGING_LEVEL=FINE
export OPENAI_API_KEY="your-openai-api-key"
export OPENROUTER_API_KEY="your-openrouter-api-key"
export POSTGRES_HOST="localhost"
export POSTGRES_DATABASE="your-database"
export POSTGRES_USER="postgres"
export POSTGRES_PASSWORD=""
export POSTGRES_PORT="5432"
export PASSWORD="your-encryption-password"
