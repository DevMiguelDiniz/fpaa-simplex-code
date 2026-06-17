# Algoritmo Simplex

## O que é Programação Linear?

Antes de entender o Simplex, é preciso entender o problema que ele resolve.

**Programação Linear (PL)** é uma técnica matemática para encontrar o melhor resultado (ótimo) num modelo cujas exigências são representadas por relações lineares. Em outras palavras: encontrar os valores de variáveis que **maximizam ou minimizam** uma função, respeitando um conjunto de **restrições**.

---

## O Problema que o Simplex Resolve

O Simplex resolve problemas da seguinte forma geral:

**Maximizar (ou Minimizar):**

```
Z = c₁x₁ + c₂x₂ + ... + cₙxₙ
```

**Sujeito a:**

```
a₁₁x₁ + a₁₂x₂ + ... + a₁ₙxₙ ≤ b₁
a₂₁x₁ + a₂₂x₂ + ... + a₂ₙxₙ ≤ b₂
         ⋮
aₘ₁x₁ + aₘ₂x₂ + ... + aₘₙxₙ ≤ bₘ

x₁, x₂, ..., xₙ ≥ 0
```

Onde:

| Símbolo       | Significado                                      |
|---------------|--------------------------------------------------|
| `x₁...xₙ`    | Variáveis de decisão (o que queremos determinar) |
| `c₁...cₙ`    | Coeficientes da função objetivo                  |
| `a₁₁...aₘₙ`  | Coeficientes das restrições                      |
| `b₁...bₘ`    | Recursos disponíveis (lado direito)              |

### Exemplo clássico — Problema da Fábrica

Uma fábrica produz cadeiras e mesas com os recursos abaixo:

| Recurso    | Cadeira | Mesa | Disponível |
|------------|---------|------|------------|
| Madeira    | 6 m²    | 4 m² | 24 m²      |
| Horas/mão  | 1 h     | 2 h  | 6 h        |
| Lucro      | R$ 5    | R$ 4 | —          |

**Formulação:**

```
Maximizar:   Z = 5x₁ + 4x₂
Sujeito a:   6x₁ + 4x₂ ≤ 24
              x₁ + 2x₂ ≤  6
              x₁, x₂  ≥  0
```

**Solução ótima:** `x₁ = 3`, `x₂ = 1,5`, `Z = 22,5`

---

## Por que não simplesmente testar todos os pontos?

O conjunto de pontos que satisfazem as restrições forma uma região chamada **poliedro convexo** (ou politopo). Em 2D, é um polígono; em dimensões maiores, um poliedro.

A teoria da PL garante que **a solução ótima sempre está num vértice** (ponto extremo) desse poliedro — nunca no interior. O problema é que o número de vértices cresce exponencialmente com o número de variáveis e restrições: para `n` variáveis e `m` restrições, pode haver `C(n+m, m)` vértices.

> Para um problema com 50 variáveis e 50 restrições, isso significa ~10²⁹ vértices possíveis. Testar todos é inviável.

O Simplex resolve isso de forma inteligente: **navega de vértice em vértice**, sempre se movendo na direção que melhora Z, até não haver mais melhoria possível.

---

## Forma Padrão — Preparação do Problema

Antes de aplicar o Simplex, o problema é convertido para a **forma padrão** com variáveis de folga:

Cada restrição `≤` recebe uma **variável de folga** `sᵢ ≥ 0` que absorve a diferença:

```
6x₁ + 4x₂ + s₁       = 24
 x₁ + 2x₂       + s₂ = 6
```

Isso transforma todas as inequações em equações. As variáveis de folga representam o recurso não utilizado.

**Sistema resultante (forma padrão):**

```
Maximizar:  Z = 5x₁ + 4x₂ + 0s₁ + 0s₂
Sujeito a:  6x₁ + 4x₂ + s₁      = 24
             x₁ + 2x₂      + s₂ =  6
            x₁, x₂, s₁, s₂ ≥ 0
```

---

## O Tableau Simplex

O algoritmo trabalha sobre uma estrutura matricial chamada **tableau**. É uma representação compacta do sistema de equações mais a função objetivo:

```
       x₁    x₂    s₁    s₂  |  RHS
Base ──────────────────────────────────
 s₁ │   6     4     1     0   |  24
 s₂ │   1     2     0     1   |   6
────┼──────────────────────────────────
  Z │  -5    -4     0     0   |   0
```

- **Colunas:** uma por variável (decisão + folga) + RHS
- **Linhas de restrição:** coeficientes das equações
- **Linha Z:** coeficientes da função objetivo (negados para maximização)
- **Coluna Base:** variável atualmente "básica" (não-zero) naquela linha

---

## O Algoritmo Passo a Passo

### Passo 1 — Solução Básica Inicial

Na largada, as variáveis de folga formam a **base inicial**:
- `x₁ = 0`, `x₂ = 0` (variáveis não-básicas)
- `s₁ = 24`, `s₂ = 6` (variáveis básicas — lidas na coluna RHS)
- `Z = 0`

Esse é o vértice origem do poliedro.

---

### Passo 2 — Escolha da Coluna Pivô (Variável Entrante)

Identifica-se o coeficiente **mais negativo** na linha Z:

```
Z │  -5    -4     0     0   |   0
         ↑
      mais negativo → coluna x₁ é a pivô
```

Isso indica que aumentar `x₁` melhora `Z` mais rapidamente. Se não houver negativos, a solução atual **é ótima** — algoritmo para.

---

### Passo 3 — Escolha da Linha Pivô (Variável Sainte) — Teste da Razão Mínima

Para descobrir qual variável básica atual deve sair da base, calcula-se a razão `RHS / coeficiente_pivô` para cada linha com coeficiente positivo na coluna pivô:

```
s₁:  24 / 6 = 4,0   ← menor razão → linha pivô
s₂:   6 / 1 = 6,0
```

A menor razão garante que **nenhuma variável fique negativa** (viabilidade). O elemento na interseção linha/coluna pivô é o **pivô**: `6`.

---

### Passo 4 — Pivoteamento (Operações Elementares de Linha)

Objetivo: transformar a coluna pivô numa coluna identidade, com `1` na linha pivô e `0` nas demais.

**4a.** Divida a linha pivô pelo valor do pivô (`6`):

```
Nova linha s₁:  [6/6  4/6  1/6  0/6 | 24/6]  →  [1  2/3  1/6  0 | 4]
```

**4b.** Elimine `x₁` das outras linhas usando a linha pivô:

```
Nova linha s₂:  [1  2  0  1 | 6] − 1·[1  2/3  1/6  0 | 4]
             =  [0  4/3  -1/6  1 | 2]

Nova linha Z:   [-5  -4  0  0 | 0] − (−5)·[1  2/3  1/6  0 | 4]
             =  [0   -4/3  5/6  0 | 20]
```

**Tableau após 1ª iteração — `x₁` entrou, `s₁` saiu:**

```
       x₁    x₂    s₁    s₂  |  RHS
Base ──────────────────────────────────
 x₁ │   1    2/3   1/6    0  |   4
 s₂ │   0    4/3  -1/6    1  |   2
────┼──────────────────────────────────
  Z │   0   -4/3   5/6    0  |  20
```

Solução atual: `x₁ = 4`, `x₂ = 0`, `Z = 20`.

---

### Passo 5 — Repetir até a Otimalidade

Ainda há um negativo na linha Z (`-4/3` em `x₂`). Repetimos:

- **Coluna pivô:** `x₂` (coeficiente `-4/3`)
- **Teste da razão:**
  - Linha `x₁`: `4 / (2/3) = 6`
  - Linha `s₂`: `2 / (4/3) = 1,5` ← menor → linha pivô

Após pivoteamento, o tableau final é:

```
       x₁    x₂    s₁    s₂  |  RHS
Base ──────────────────────────────────
 x₁ │   1     0    1/4  -1/2 |   3
 x₂ │   0     1   -1/8   3/4 |  1,5
────┼──────────────────────────────────
  Z │   0     0   3/4    1   | 22,5
```

Linha Z sem negativos → **solução ótima encontrada!**

**Resultado:** `x₁ = 3`, `x₂ = 1,5`, `Z = 22,5`

---

## Resumo do Fluxo do Algoritmo

```
┌─────────────────────────────────────────┐
│         INÍCIO                          │
│  Montar tableau com variáveis de folga  │
└───────────────┬─────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────┐
│  Existe coeficiente negativo na linha Z?│
└───────┬───────────────────┬─────────────┘
       NÃO                 SIM
        │                   │
        ▼                   ▼
   ┌─────────┐    ┌──────────────────────┐
   │  ÓTIMO  │    │ Escolhe coluna pivô  │
   └─────────┘    │ (mais negativo em Z) │
                  └──────────┬───────────┘
                             │
                             ▼
                  ┌──────────────────────┐
                  │ Teste da razão mínima│
                  │ → escolhe linha pivô │
                  └──────────┬───────────┘
                             │
                   Razão existe?
                   ┌────┴────┐
                  NÃO       SIM
                   │         │
                   ▼         ▼
            ┌──────────┐ ┌────────────────┐
            │ILIMITADO │ │  Pivoteamento  │
            └──────────┘ │ (op. de linha) │
                         └───────┬────────┘
                                 │
                                 └──── (volta ao início)
```

---

## Casos Especiais

| Situação | O que acontece no tableau |
|---|---|
| **Solução ótima** | Nenhum coeficiente negativo na linha Z |
| **Problema ilimitado** | Coluna pivô escolhida, mas nenhum coeficiente positivo nas linhas → Z pode crescer indefinidamente |
| **Problema inviável** | Não existe solução que satisfaça todas as restrições simultaneamente — detectado com o Método das Duas Fases ou Método Big-M |
| **Degenerescência** | Razão mínima = 0; base muda sem mover no espaço — pode causar ciclagem (resolvida com a regra de Bland) |

---

## Complexidade

| Caso | Complexidade |
|---|---|
| **Pior caso teórico** | Exponencial — O(2ⁿ) |
| **Prática** | Polinomial na maioria dos problemas reais |

Embora existam exemplos patológicos (como o Exemplo de Klee-Minty) onde o Simplex visita exponencialmente muitos vértices, na prática ele é extremamente eficiente, resolvendo problemas com milhares de variáveis em segundos.

Para garantias polinomiais, existem métodos de pontos interiores (Karmarkar, 1984), mas o Simplex continua sendo o algoritmo mais usado na indústria pela sua eficiência prática e facilidade de interpretação.

---

## Referências

- Dantzig, G. B. (1947). *Maximization of a linear function of variables subject to linear inequalities.*
- Bazaraa, M. S., Jarvis, J. J., & Sherali, H. D. (2010). *Linear Programming and Network Flows* (4ª ed.). Wiley.
- Hillier, F. S., & Lieberman, G. J. (2015). *Introduction to Operations Research* (10ª ed.). McGraw-Hill.
