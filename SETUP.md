# Como rodar e configurar

## 1. Supabase Storage (fotos das receitas)

1. Crie um projeto em <https://supabase.com>.
2. **Storage → New bucket**: nome `receitas`, marque **Public bucket**.
   Sem isso as fotos sobem, mas o navegador não consegue exibi-las.
3. **Project Settings → API Keys**: copie a chave **secreta** —
   `sb_secret_…` nos painéis novos, ou `service_role` (começa com `eyJ`)
   na aba Legacy. A chave *publishable* **não serve**: ela respeita RLS e
   não escreve no Storage.
   Ela é secreta e fica **só no backend** — nunca em variável `NEXT_PUBLIC_`.
4. Defina as variáveis:

```
SUPABASE_URL=https://xxxxxxxxxxxx.supabase.co
SUPABASE_SERVICE_KEY=<service_role key>
SUPABASE_BUCKET=receitas
```

Sem essas variáveis a API continua funcionando; só o envio de foto é
recusado, com uma mensagem explicando o que falta.

### Duas confusões comuns

**Bucket ≠ projeto.** `SUPABASE_BUCKET` é o nome que aparece em
*Storage → Buckets*, não o nome do projeto (que fica na barra superior).

**`Invalid Compact JWS`** significa que a chave não foi reconhecida —
e aparece igual para chave vazia, truncada ou em formato inesperado.
O upload manda a chave nos headers `apikey` **e** `Authorization`,
porque as chaves novas (`sb_secret_…`) só são aceitas no `apikey`,
enquanto o `Authorization` sozinho tenta lê-las como JWT.
Já `row-level security policy` quer dizer o contrário: a chave foi lida,
mas é a publishable, que não tem permissão de escrita.

### Testando antes de mexer no Railway

```bash
./scripts/testar-supabase.sh /caminho/para/uma/foto.jpg
```

O script confere o bucket, envia a imagem e verifica se ela abre
publicamente — separando "Supabase mal configurado" de "aplicação com
problema". A chave secreta é digitada de forma oculta: não vai para a
tela nem para o histórico do shell.

## 2. Primeiro administrador

As rotas de escrita de receita exigem um administrador, e a rota que cria
administrador também — então o primeiro nasce de variáveis de ambiente,
na subida da aplicação:

```
ADMIN_NAME=Administrador
ADMIN_EMAIL=admin@seudominio.com
ADMIN_PASSWORD=uma-senha-boa
```

Comportamento na subida:

| Situação do e-mail | O que acontece |
|---|---|
| não existe | cria a conta já como administrador |
| existe como usuário comum | **promove** a conta (mantém id, senha e favoritos) |
| já é administrador | não faz nada |

Na promoção a senha **não** é trocada: a conta continua entrando com a
senha que já tinha, e `ADMIN_PASSWORD` é ignorada. Isso vale para o caso
comum de você ter se cadastrado pela tela antes de virar admin.

Depois de logado, esse admin pode criar outros por `POST /administrador`.

### Promover alguém direto pelo banco

Administrador herda de Usuario com estratégia JOINED, então ser admin é
só ter a linha em `tb_administrador` com o mesmo id:

```sql
INSERT INTO tb_administrador (id)
SELECT id FROM tb_usuario WHERE email = 'fulano@exemplo.com';
```

E para conferir quem é o quê:

```sql
SELECT u.id, u.email,
       CASE WHEN a.id IS NULL THEN 'USER' ELSE 'ADMIN' END AS papel
FROM tb_usuario u
LEFT JOIN tb_administrador a ON a.id = u.id;
```

## 3. Demais variáveis

```
CORS_ALLOWED_ORIGINS=http://localhost:3000,https://seu-front.vercel.app
JWT_SECRET=<string longa e aleatoria>
```

`CORS_ALLOWED_ORIGINS` aceita lista separada por vírgula e precisa conter
a origem exata do front (com https, sem barra no final).

## 4. Frontend

No Vercel, defina apenas:

```
NEXT_PUBLIC_API_URL=https://seu-backend.up.railway.app
```

Sem `:8080` — o domínio público do Railway responde em HTTPS na porta 443.

## Rotas

| Método | Rota | Quem pode |
|---|---|---|
| `GET` | `/recipes?filter=` | qualquer um |
| `GET` | `/recipes/{id}` | qualquer um |
| `POST` | `/recipes` | administrador |
| `PUT` | `/recipes/{id}` | administrador |
| `DELETE` | `/recipes/{id}` | administrador |
| `GET` | `/favoritos` | logado |
| `POST` | `/favoritos/{receitaId}` | logado |
| `DELETE` | `/favoritos/{receitaId}` | logado |
| `POST` | `/auth/register` | qualquer um |
| `POST` | `/auth/login` | qualquer um |
| `GET` | `/auth/me` | logado |
| `/usuarios/**` | | dono da conta ou administrador |
| `/administrador/**` | | administrador |

Valores de `filter`: `all`, `favorites`, `quick` (até 30 min), `sweet`,
`savory`, `weekly` (mais favoritadas).

`POST` e `PUT` de receita são `multipart/form-data` com os campos
`nome`, `desc`, `tempo`, `categoria` (opcional, `doce` ou `salgada`) e
`imagem` (opcional). No `PUT`, não enviar `imagem` preserva a foto atual.

## Pendência conhecida

As senhas são gravadas e comparadas em texto puro. Para um trabalho de
apresentação funciona, mas não use esse banco com senha real de ninguém.
O ajuste é trocar por BCrypt no `UsuarioService`.
