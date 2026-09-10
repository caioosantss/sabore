#!/usr/bin/env bash
#
# Testa o envio de uma imagem para o Supabase Storage, do mesmo jeito que
# a API faz. Serve para separar "o Supabase esta mal configurado" de
# "a aplicacao esta com problema".
#
#   ./scripts/testar-supabase.sh /caminho/da/foto.jpg
#
# A chave secreta e digitada de forma oculta: nao aparece na tela, nao
# fica no historico do shell e nao entra em nenhum arquivo.

set -uo pipefail

FOTO="${1:-}"

if [ -z "$FOTO" ] || [ ! -f "$FOTO" ]; then
    echo "Uso: $0 /caminho/para/uma/imagem.jpg"
    exit 1
fi

# --- dados do projeto ---------------------------------------------------

read -rp "SUPABASE_URL (ex.: https://xxxx.supabase.co): " URL
read -rp "SUPABASE_BUCKET: " BUCKET

URL="${URL%/}"   # tira a barra final, se houver

echo -n "SUPABASE_SERVICE_KEY (a digitacao fica oculta): "
read -rs KEY
echo

if [ -z "$URL" ] || [ -z "$BUCKET" ] || [ -z "$KEY" ]; then
    echo "Faltou preencher algum campo."
    exit 1
fi

case "$KEY" in
    sb_publishable_*)
        echo
        echo "AVISO: essa e a chave PUBLISHABLE, que nao escreve no Storage."
        echo "       Use a secreta (sb_secret_...) ou a service_role (eyJ...)."
        echo
        ;;
esac

# --- 1. o bucket existe e e publico? ------------------------------------

echo "== 1. Conferindo o bucket (nao usa chave) =="

RESP=$(curl -s --max-time 20 "$URL/storage/v1/object/public/$BUCKET/__inexistente__.png")

case "$RESP" in
    *NoSuchBucket*|*"Bucket not found"*)
        echo "   FALHOU: bucket '$BUCKET' nao existe (ou o nome esta diferente)."
        echo "   Crie em Storage > New bucket, marcando 'Public bucket'."
        exit 1
        ;;
    *"not_found"*|*"Object not found"*)
        echo "   OK: bucket existe e esta publico."
        ;;
    *)
        echo "   Resposta inesperada: $RESP"
        echo "   Se falar em 'row-level security', o bucket existe mas e privado."
        ;;
esac

# --- 2. envia a imagem --------------------------------------------------

echo "== 2. Enviando a imagem =="

NOME="teste-$(date +%s)-$(basename "$FOTO")"
TIPO=$(file --mime-type -b "$FOTO")

echo "   arquivo: $(basename "$FOTO")  tipo: $TIPO"

CODIGO=$(curl -s -o /tmp/supa-resposta.txt -w "%{http_code}" --max-time 60 \
    -X POST "$URL/storage/v1/object/$BUCKET/$NOME" \
    -H "apikey: $KEY" \
    -H "Authorization: Bearer $KEY" \
    -H "Content-Type: $TIPO" \
    -H "x-upsert: true" \
    --data-binary "@$FOTO")

if [ "$CODIGO" -lt 200 ] || [ "$CODIGO" -ge 300 ]; then
    echo "   FALHOU (HTTP $CODIGO): $(cat /tmp/supa-resposta.txt)"
    echo
    case "$(cat /tmp/supa-resposta.txt)" in
        *"row-level security"*)
            echo "   -> A chave foi reconhecida, mas nao tem permissao de escrita."
            echo "      E a publishable. Use a SECRETA (sb_secret_... ou service_role)."
            ;;
        *"Invalid Compact JWS"*)
            echo "   -> A chave nao foi reconhecida: veio vazia ou truncada."
            echo "      Confira com: echo \${#KEY}"
            ;;
        *"AccessDenied"*|*Unauthorized*)
            echo "   -> Chave sem permissao. Use a SECRETA, nao a publishable."
            ;;
        *NoSuchBucket*|*"Bucket not found"*)
            echo "   -> Nome do bucket errado."
            ;;
    esac
    rm -f /tmp/supa-resposta.txt
    exit 1
fi

echo "   OK (HTTP $CODIGO)"

PUBLICA="$URL/storage/v1/object/public/$BUCKET/$NOME"

# --- 3. a imagem abre publicamente? -------------------------------------

echo "== 3. Conferindo se a imagem abre =="

LEITURA=$(curl -s -o /dev/null -w "%{http_code} %{content_type}" --max-time 20 "$PUBLICA")

echo "   $LEITURA"

case "$LEITURA" in
    2*image/*)
        echo
        echo "TUDO CERTO. Use estes valores no Railway:"
        echo "   SUPABASE_URL=$URL"
        echo "   SUPABASE_BUCKET=$BUCKET"
        echo "   SUPABASE_SERVICE_KEY=<a chave secreta que voce digitou>"
        echo
        echo "URL da imagem enviada:"
        echo "   $PUBLICA"
        ;;
    *)
        echo
        echo "O envio funcionou, mas a imagem nao abre publicamente."
        echo "O bucket provavelmente NAO esta marcado como 'Public bucket'."
        echo "Storage > o bucket > Settings > Public bucket."
        ;;
esac

rm -f /tmp/supa-resposta.txt
