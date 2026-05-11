---
name: Agricultural AI Design System
colors:
  surface: '#f9f9f9'
  surface-dim: '#dadada'
  surface-bright: '#f9f9f9'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f3f3f3'
  surface-container: '#eeeeee'
  surface-container-high: '#e8e8e8'
  surface-container-highest: '#e2e2e2'
  on-surface: '#1a1c1c'
  on-surface-variant: '#40493d'
  inverse-surface: '#2f3131'
  inverse-on-surface: '#f1f1f1'
  outline: '#707a6c'
  outline-variant: '#bfcaba'
  surface-tint: '#1b6d24'
  primary: '#0d631b'
  on-primary: '#ffffff'
  primary-container: '#2e7d32'
  on-primary-container: '#cbffc2'
  inverse-primary: '#88d982'
  secondary: '#795900'
  on-secondary: '#ffffff'
  secondary-container: '#fec330'
  on-secondary-container: '#6f5100'
  tertiary: '#1f6223'
  on-tertiary: '#ffffff'
  tertiary-container: '#3a7b39'
  on-tertiary-container: '#c8ffbf'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#a3f69c'
  primary-fixed-dim: '#88d982'
  on-primary-fixed: '#002204'
  on-primary-fixed-variant: '#005312'
  secondary-fixed: '#ffdfa0'
  secondary-fixed-dim: '#f8bd2a'
  on-secondary-fixed: '#261a00'
  on-secondary-fixed-variant: '#5c4300'
  tertiary-fixed: '#acf4a4'
  tertiary-fixed-dim: '#91d78a'
  on-tertiary-fixed: '#002203'
  on-tertiary-fixed-variant: '#0c5216'
  background: '#f9f9f9'
  on-background: '#1a1c1c'
  surface-variant: '#e2e2e2'
typography:
  display-lg:
    fontFamily: Work Sans
    fontSize: 57px
    fontWeight: '400'
    lineHeight: 64px
    letterSpacing: -0.25px
  headline-lg:
    fontFamily: Work Sans
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
    letterSpacing: 0px
  headline-md:
    fontFamily: Work Sans
    fontSize: 28px
    fontWeight: '500'
    lineHeight: 36px
    letterSpacing: 0px
  title-lg:
    fontFamily: Work Sans
    fontSize: 22px
    fontWeight: '500'
    lineHeight: 28px
    letterSpacing: 0px
  body-lg:
    fontFamily: Work Sans
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: 0.5px
  body-md:
    fontFamily: Work Sans
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: 0.25px
  label-lg:
    fontFamily: Work Sans
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
    letterSpacing: 0.1px
  label-sm:
    fontFamily: Work Sans
    fontSize: 11px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.5px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 8px
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 32px
  gutter: 16px
  margin-mobile: 16px
  margin-tablet: 24px
---

## Brand & Style
This design system is engineered for the intersection of high-technology and field-level agriculture. The brand personality is rooted in reliability and ecological stewardship, aiming to evoke a sense of calm efficiency for farmers and agronomists. 

The style is **Corporate / Modern**, adhering to the functional rigor of Material 3 while incorporating organic visual cues. It prioritizes clarity and high legibility to ensure the UI remains usable in high-glare outdoor environments. The interface utilizes generous whitespace and a structured hierarchy to distill complex AI data into actionable insights without overwhelming the user.

## Colors
The color palette is derived from the natural lifecycle of crops. **Forest Green** serves as the primary brand anchor, representing growth and health. **Golden Yellow** is utilized as a secondary accent color to draw attention to critical harvest data and AI-driven warnings. 

The system defaults to a **Light Mode** to maximize contrast and visibility under direct sunlight. Surfaces use a mix of pure white and subtle light grays to differentiate between global navigation and content containers. Tonal palettes should be generated from these anchors to provide accessible color variants for state changes and container backgrounds.

## Typography
The design system employs **Work Sans** across all levels. This choice provides a grounded, professional character with exceptional legibility at both large and small scales. 

Headline levels use medium and semi-bold weights to establish a clear information hierarchy, while body text maintains a generous line height to ensure readability during active field use. Numerical data—crucial for an AI app—should be rendered with `label-lg` or `headline-md` to ensure metrics like soil moisture or yield projections are immediately scannable.

## Layout & Spacing
The layout follows a **fluid grid** model based on an 8px base unit. For mobile devices, a 4-column grid with 16px margins is standard. For tablet and desktop views, a 12-column grid is used with 24px margins to accommodate complex data dashboards.

Spacing is designed to be airy but disciplined. Use `lg` (24px) padding for primary card containers to give AI insights breathing room. Use `sm` (8px) for internal element grouping within components like list items or input fields to maintain a compact, efficient feel for data-heavy views.

## Elevation & Depth
In alignment with Material 3, elevation is primarily conveyed through **tonal layers** rather than heavy shadows. Different levels of the UI are represented by varying surface container colors. 

When depth is required for floating elements (like FABs or dropdowns), use **ambient shadows**: soft, diffused shadows with a low-opacity Forest Green tint (`#2E7D32` at 8-12% opacity) to maintain a nature-inspired warmth. This prevents the UI from feeling "cold" or overly clinical while clearly defining the stack order of interactive elements.

## Shapes
The shape language uses a **Rounded** philosophy to echo organic forms found in nature. 
- Standard components (Buttons, Inputs) use a `0.5rem` (8px) radius.
- Large containers (Cards, Sheets) use a `1rem` (16px) radius.
- Full rounding (Pill-shaped) is reserved for interactive Chips and status indicators to make them feel distinct from structural content. 

This softened geometry balances the precision of the AI data with the approachable, nature-centric mission of the product.

## Components

### Buttons
Primary buttons use a solid **Forest Green** fill with white text. They should have high tactile visibility. Secondary buttons use an outlined style with a 1px Forest Green border.

### Chips
Used for filtering crop types or selecting dates. Use pill-shaped containers. "Active" chips should use a light Forest Green tonal fill, while "Inactive" chips use a subtle neutral stroke.

### Cards
Cards are the primary vehicle for AI insights. They should use a subtle 1px border in a light neutral tone instead of a shadow to maintain a clean, modern look. The card header should clearly display the "Confidence Score" of the AI using the **Golden Yellow** accent.

### Input Fields
Filled text fields are preferred for field use to provide a larger tap target. The bottom indicator line and active label should use Forest Green to signify focus.

### List Items
Data lists should be highly scannable with 56px minimum height for mobile rows. Use trailing icons or labels to show real-time sensor status (e.g., a green dot for "Online").

### AI Insight Banners
A specialized component for urgent AI notifications. Use a soft Golden Yellow background with a dark Forest Green icon to signal "Alert" or "Recommendation" without causing unnecessary alarm.