

# Frontend Development Skill Guide

A comprehensive reference for building production-grade, visually distinctive frontend interfaces.

---

## 1. Design Thinking (Before You Write Any Code)

Always answer these before touching an editor:

| Question | Why It Matters |
|---|---|
| Who is the user? | Shapes tone, density, accessibility needs |
| What is the ONE action they should take? | Drives layout hierarchy |
| What emotion should the page evoke? | Guides color, motion, typography |
| What makes it unforgettable? | The differentiating design decision |

### Aesthetic Directions (pick one and commit)

| Style | Characteristics |
|---|---|
| **Brutalist** | Raw, bold borders, monospace, high contrast |
| **Editorial** | Large type, white space, magazine-like grid |
| **Retro-futuristic** | Scanlines, glow, terminal greens, neon |
| **Luxury / Refined** | Serif fonts, gold accents, generous spacing |
| **Playful / Toy-like** | Rounded corners, bright primaries, bouncy motion |
| **Organic / Natural** | Earthy tones, soft shapes, hand-drawn feel |
| **Industrial** | Grids, mechanical type, desaturated palette |
| **Art Deco** | Geometric patterns, symmetry, metallic tones |

> **Rule**: Bold maximalism and refined minimalism both work — the key is **intentionality**, not intensity.

---

## 2. Typography

### Do
- Pair a **distinctive display font** with a **refined body font**
- Use Google Fonts or variable fonts for performance
- Set fluid type with `clamp()` for responsive sizing
- Use `font-feature-settings` for ligatures and numerals

### Don't
- ❌ Arial, Roboto, Inter, System UI (too generic)
- ❌ More than 2–3 font families
- ❌ Body text below 16px

### Recommended Font Pairings

| Display | Body | Mood |
|---|---|---|
| Playfair Display | Lora | Editorial luxury |
| Syne | DM Sans | Modern / geometric |
| Bebas Neue | Source Serif 4 | Bold / industrial |
| Cormorant Garamond | Jost | Refined / elegant |
| Fragment Mono | Figtree | Technical / minimal |
| Fraunces | Epilogue | Warm / literary |

```css
/* Example: fluid type scale */
:root {
  --text-sm:   clamp(0.875rem, 1.5vw, 1rem);
  --text-base: clamp(1rem,     2vw,   1.125rem);
  --text-lg:   clamp(1.25rem,  3vw,   1.5rem);
  --text-xl:   clamp(1.75rem,  5vw,   3rem);
  --text-2xl:  clamp(2.5rem,   8vw,   5rem);
}
```

---

## 3. Color & Theme

### Core Principles
- Use CSS custom properties for every color value
- Choose a **dominant color** + **sharp accent** + **neutral base**
- Avoid evenly-distributed palettes — contrast creates hierarchy

### Color System Template

```css
:root {
  /* Base */
  --color-bg:       #0a0a0a;
  --color-surface:  #141414;
  --color-border:   #2a2a2a;

  /* Text */
  --color-text:     #f0ece4;
  --color-muted:    #888580;

  /* Brand */
  --color-primary:  #e8c547;   /* dominant accent */
  --color-secondary:#c47a3a;   /* supporting tone */

  /* States */
  --color-success:  #5cb85c;
  --color-error:    #d9534f;
}
```

### Avoid
- ❌ Purple gradients on white (over-used AI aesthetic)
- ❌ Flat `#ffffff` backgrounds with no depth
- ❌ Generic blue (#007bff) as the only accent

---

## 4. Layout & Spatial Composition

### Techniques
- **CSS Grid** for page-level layout
- **Flexbox** for component-level alignment
- **Asymmetry** — break the predictable centered layout
- **Overlap** — elements that cross grid lines add depth
- **Generous negative space** OR **controlled density** (pick one)

### Grid Template

```css
.page-grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: clamp(1rem, 2vw, 2rem);
  max-width: 1440px;
  margin: 0 auto;
  padding-inline: clamp(1.5rem, 5vw, 5rem);
}

/* Asymmetric hero */
.hero-text  { grid-column: 1 / 8; }
.hero-media { grid-column: 7 / 13; } /* intentional overlap */
```

---

## 5. Motion & Animation

### Principles
- **One well-orchestrated entrance** beats scattered micro-interactions
- Staggered reveals on page load feel professional
- Hover states should **surprise** — not just opacity fade
- Prefer **CSS transitions** for simple states; **JS (GSAP / Motion)** for sequences

### Staggered Entrance (CSS only)

```css
.card {
  opacity: 0;
  transform: translateY(24px);
  animation: fadeUp 0.6s ease forwards;
}
.card:nth-child(1) { animation-delay: 0.1s; }
.card:nth-child(2) { animation-delay: 0.2s; }
.card:nth-child(3) { animation-delay: 0.3s; }

@keyframes fadeUp {
  to { opacity: 1; transform: translateY(0); }
}
```

### Scroll-triggered (Intersection Observer)

```js
const observer = new IntersectionObserver(
  entries => entries.forEach(e => {
    if (e.isIntersecting) e.target.classList.add('visible');
  }),
  { threshold: 0.15 }
);
document.querySelectorAll('.reveal').forEach(el => observer.observe(el));
```

```css
.reveal            { opacity: 0; transform: translateY(32px); transition: all 0.7s ease; }
.reveal.visible    { opacity: 1; transform: none; }
```

---

## 6. Backgrounds & Visual Depth

Never use flat solid colors. Create **atmosphere**:

### Gradient Mesh

```css
background: 
  radial-gradient(ellipse at 20% 50%, rgba(232,197,71,0.15) 0%, transparent 60%),
  radial-gradient(ellipse at 80% 20%, rgba(196,122,58,0.12) 0%, transparent 50%),
  #0a0a0a;
```

### Noise Texture Overlay

```css
.noise::before {
  content: '';
  position: fixed; inset: 0;
  background-image: url("data:image/svg+xml,..."); /* SVG noise */
  opacity: 0.04;
  pointer-events: none;
  z-index: 9999;
}
```

### Geometric Pattern

```css
background-image:
  linear-gradient(rgba(255,255,255,.03) 1px, transparent 1px),
  linear-gradient(90deg, rgba(255,255,255,.03) 1px, transparent 1px);
background-size: 40px 40px;
```

---

## 7. Component Patterns

### Button System

```css
.btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.75rem;
  font-family: var(--font-display);
  font-size: var(--text-sm);
  letter-spacing: 0.08em;
  text-transform: uppercase;
  border: 2px solid var(--color-primary);
  background: transparent;
  color: var(--color-primary);
  cursor: pointer;
  transition: all 0.25s ease;
}
.btn:hover {
  background: var(--color-primary);
  color: var(--color-bg);
  transform: translate(-3px, -3px);
  box-shadow: 3px 3px 0 var(--color-primary);
}
```

### Card with Depth

```css
.card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  padding: 2rem;
  position: relative;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}
.card:hover {
  transform: translateY(-6px);
  box-shadow: 0 20px 60px rgba(0,0,0,0.4);
}
.card::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(255,255,255,0.03), transparent);
  pointer-events: none;
}
```

---

## 8. Responsive Design

```css
/* Mobile-first breakpoints */
:root {
  --bp-sm:  480px;
  --bp-md:  768px;
  --bp-lg:  1024px;
  --bp-xl:  1280px;
  --bp-2xl: 1536px;
}

/* Usage */
@media (min-width: 768px)  { /* tablet + */ }
@media (min-width: 1024px) { /* desktop + */ }
```

### Fluid Spacing

```css
:root {
  --space-xs: clamp(0.5rem,  1vw,  0.75rem);
  --space-sm: clamp(0.75rem, 2vw,  1.25rem);
  --space-md: clamp(1.25rem, 3vw,  2rem);
  --space-lg: clamp(2rem,    5vw,  4rem);
  --space-xl: clamp(3.5rem,  8vw,  7rem);
}
```

---

## 9. Accessibility Checklist

- [ ] Color contrast ratio ≥ 4.5:1 for body text (use [contrast checker](https://webaim.org/resources/contrastchecker/))
- [ ] All interactive elements reachable by keyboard (`Tab` / `Enter`)
- [ ] Focus rings visible (don't just `outline: none`)
- [ ] Images have meaningful `alt` text
- [ ] Semantic HTML: `<main>`, `<nav>`, `<header>`, `<footer>`, `<article>`
- [ ] ARIA labels on icon-only buttons
- [ ] Reduced motion respected

```css
@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    transition-duration: 0.01ms !important;
  }
}
```

---

## 10. Performance Best Practices

| Technique | Impact |
|---|---|
| `font-display: swap` on web fonts | Prevents invisible text flash |
| `loading="lazy"` on images | Defers off-screen image loads |
| CSS `will-change: transform` on animated elements | GPU layer promotion |
| Inline critical CSS | Eliminates render-blocking |
| `preconnect` for Google Fonts | Faster font fetch |
| SVG icons instead of icon fonts | Smaller, scalable, styleable |

```html
<!-- Fast Google Fonts setup -->
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
```

---

## 11. Tooling Quick Reference

| Tool | Purpose |
|---|---|
| **Vite** | Fastest dev server + bundler |
| **Tailwind CSS** | Utility-first CSS |
| **GSAP** | Professional animation |
| **Motion (Framer)** | React animation library |
| **Radix UI** | Accessible headless components |
| **shadcn/ui** | Styled Radix components |
| **Lucide** | Clean SVG icon set |
| **CSS Nesting** | Native (no preprocessor needed in 2025) |

---

## 12. Anti-Patterns to Avoid

| ❌ Don't | ✅ Do Instead |
|---|---|
| Purple gradient hero on white | Commit to a strong, original palette |
| Inter / Roboto everywhere | Pick a characterful display font |
| Centered layout, 3 equal columns | Asymmetric grids, intentional hierarchy |
| Opacity fade hover on everything | Surprising, context-specific hover states |
| Flat `#ffffff` background | Gradient mesh, subtle texture, depth |
| `box-shadow: 0 2px 4px rgba(0,0,0,0.1)` | Dramatic shadows that match the aesthetic |
| 8 animations firing at once | One orchestrated entrance sequence |

---

*Generated with the frontend-design Claude skill.*
