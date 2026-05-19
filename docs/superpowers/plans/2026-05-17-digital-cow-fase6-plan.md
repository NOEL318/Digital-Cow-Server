# Fase 6 — Simplificacion, Accesibilidad y UX Unificada (plan maestro)

> Para agentes: usar `superpowers:subagent-driven-development` o `superpowers:executing-plans`. Pasos marcados con `- [ ]`. Estricto: el usuario manda commits manualmente — los pasos de plan NO incluyen `git commit` ni `git add`.

**Spec:** `docs/superpowers/specs/2026-05-17-digital-cow-fase6-design.md`
**Meta:** simplificar IA, hacer accesible para baja alfabetizacion, añadir catalogo de medicamentos con escaneo, graficas comparativas por animal, foto omnipresente, integrar compra/venta, migrar correos a Resend, parametrizar secretos.
**Arquitectura:** sin cambios de stack (Java 21 + Spring Boot 3.3 + React 18 + Vite). Refactor en frontend y aditivos en backend (3 migraciones, 4 modulos nuevos). 10 sub-iteraciones independientes y desplegables por separado.
**Stack:** ver `project-digital-cow-status` y el spec.

---

## Estrategia de descomposicion

Cada sub-iteracion produce software funcional sobre la anterior. Iteracion 6.1 es la unica con riesgo bajo y desbloquea todo lo demas (cambia el shell sin tocar funcionalidad). Las iteraciones se detallan en pasos bite-sized **just-in-time** justo antes de ejecutarse para evitar derivar de codigo que aun no existe. Este plan inicia con la 6.1 totalmente detallada; las siguientes tienen objetivo, archivos y criterio de cierre claros, y se expanden en pasos antes de su ejecucion.

---

## Iteracion 6.1 — Shell de 5 destinos + redirects

**Goal:** reemplazar `AppLayout`/`Sidebar` por un shell con cinco destinos (Inicio, Animales, Hacer una nota, Panel, Ajustes); bottom nav fijo en movil, sidebar lateral en escritorio. Todas las rutas viejas siguen funcionando via redirect. Sin cambios funcionales en paginas individuales.

**Archivos:**
- Crear `frontend/src/components/app-shell.tsx`
- Crear `frontend/src/components/bottom-nav.tsx`
- Crear `frontend/src/components/desktop-sidebar.tsx` (reemplaza `sidebar.tsx`; con 5 destinos)
- Crear `frontend/src/pages/inicio/InicioPage.tsx` (stub que renderiza el dashboard actual con titulo "Inicio")
- Crear `frontend/src/pages/panel/PanelIndexPage.tsx` (hub: tarjetas a Salud, Alimentacion, Dinero, Reproduccion, Produccion)
- Crear `frontend/src/pages/hacer-nota/HacerNotaPage.tsx` (stub con grid de 12 acciones; cada accion abre el form existente correspondiente en una ruta vieja; los wizards reales vienen en 6.3)
- Crear `frontend/src/pages/ajustes/AjustesIndexPage.tsx` (hub: tarjetas a Perfil, Cuenta, Ranchos, Equipo, Categorias, Medicamentos, Idioma/Tema)
- Modificar `frontend/src/app/router.tsx` (nuevas rutas + redirects de las viejas)
- Modificar `frontend/src/pages/AppLayout.tsx` (delegar en `AppShell`)
- Modificar `frontend/src/components/sidebar.tsx` (queda como wrapper que reexporta `DesktopSidebar` para compatibilidad si algun test lo importa; o se elimina si nadie lo usa)
- Modificar `frontend/public/locales/es/common.json` y `frontend/public/locales/en/common.json` (claves nuevas en `nav`)
- Test: `frontend/src/components/__tests__/bottom-nav.test.tsx`
- Test: `frontend/src/components/__tests__/app-shell.test.tsx`

### Pasos

- [ ] **Paso 1 — añadir claves i18n en `common.json` (ES y EN)**

Editar `frontend/public/locales/es/common.json`: bloque `nav` queda:
```json
"nav": {
  "inicio": "Inicio",
  "animales": "Animales",
  "hacerNota": "Hacer una nota",
  "panel": "Panel",
  "panelSalud": "Salud",
  "panelAlimentacion": "Alimentacion",
  "panelDinero": "Dinero",
  "panelReproduccion": "Reproduccion",
  "panelProduccion": "Produccion",
  "ajustes": "Ajustes",
  "ajustesPerfil": "Perfil",
  "ajustesCuenta": "Cuenta",
  "ajustesRanchos": "Ranchos",
  "ajustesEquipo": "Equipo",
  "ajustesCategorias": "Categorias de dinero",
  "ajustesMedicamentos": "Catalogo de medicamentos",
  "ajustesIdiomaTema": "Idioma y tema",
  "logout": "Salir",
  "dashboard": "Tablero",
  "animals": "Animales",
  "ranches": "Ranchos",
  "team": "Equipo",
  "settings": "Configuracion",
  "management": "Gestion"
}
```

Editar `frontend/public/locales/en/common.json` con las traducciones equivalentes en ingles:
```json
"nav": {
  "inicio": "Home",
  "animales": "Animals",
  "hacerNota": "Make a note",
  "panel": "Panel",
  "panelSalud": "Health",
  "panelAlimentacion": "Feeding",
  "panelDinero": "Money",
  "panelReproduccion": "Reproduction",
  "panelProduccion": "Production",
  "ajustes": "Settings",
  "ajustesPerfil": "Profile",
  "ajustesCuenta": "Account",
  "ajustesRanchos": "Ranches",
  "ajustesEquipo": "Team",
  "ajustesCategorias": "Money categories",
  "ajustesMedicamentos": "Medicine catalog",
  "ajustesIdiomaTema": "Language and theme",
  "logout": "Sign out",
  "dashboard": "Dashboard",
  "animals": "Animals",
  "ranches": "Ranches",
  "team": "Team",
  "settings": "Settings",
  "management": "Management"
}
```

- [ ] **Paso 2 — crear `BottomNav`**

Crear `frontend/src/components/bottom-nav.tsx`:
```tsx
import { NavLink } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Home, Beef, PencilLine, LayoutGrid, Settings } from 'lucide-react';

/**
 * Barra inferior fija de cinco destinos (solo movil).
 * El destino central "Hacer una nota" se renderiza como boton elevado (FAB-like).
 */
export function BottomNav() {
  const { t } = useTranslation('common');
  const linkClass = ({ isActive }: { isActive: boolean }) =>
    `flex flex-col items-center justify-center text-xs gap-0.5 flex-1 py-2 ${isActive ? 'text-primary font-semibold' : 'text-muted-foreground'}`;

  return (
    <nav
      className="fixed bottom-0 inset-x-0 z-40 bg-background border-t md:hidden print:hidden"
      aria-label={t('nav.panel')}
    >
      <div className="flex items-stretch">
        <NavLink to="/inicio" className={linkClass} end>
          <Home className="h-5 w-5" aria-hidden />
          <span>{t('nav.inicio')}</span>
        </NavLink>
        <NavLink to="/animales" className={linkClass}>
          <Beef className="h-5 w-5" aria-hidden />
          <span>{t('nav.animales')}</span>
        </NavLink>
        <NavLink
          to="/hacer-nota"
          className={({ isActive }) =>
            `flex flex-col items-center justify-center text-xs gap-0.5 flex-1 py-2 ${isActive ? 'text-primary-foreground' : 'text-primary-foreground'}`
          }
        >
          <span className="-mt-6 flex h-12 w-12 items-center justify-center rounded-full bg-primary shadow-lg">
            <PencilLine className="h-6 w-6 text-primary-foreground" aria-hidden />
          </span>
          <span className="text-foreground">{t('nav.hacerNota')}</span>
        </NavLink>
        <NavLink to="/panel" className={linkClass}>
          <LayoutGrid className="h-5 w-5" aria-hidden />
          <span>{t('nav.panel')}</span>
        </NavLink>
        <NavLink to="/ajustes" className={linkClass}>
          <Settings className="h-5 w-5" aria-hidden />
          <span>{t('nav.ajustes')}</span>
        </NavLink>
      </div>
    </nav>
  );
}
```

- [ ] **Paso 3 — crear `DesktopSidebar`**

Crear `frontend/src/components/desktop-sidebar.tsx`:
```tsx
import { NavLink } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Home, Beef, PencilLine, LayoutGrid, Settings } from 'lucide-react';

/**
 * Sidebar lateral fija para escritorio. Cinco destinos: Inicio, Animales,
 * Hacer una nota, Panel, Ajustes. Visible solo en md y mayores.
 */
export function DesktopSidebar() {
  const { t } = useTranslation('common');
  const itemBase = 'flex items-center gap-3 px-3 py-2 rounded-md text-sm';
  const link = ({ isActive }: { isActive: boolean }) =>
    `${itemBase} ${isActive ? 'bg-accent font-semibold' : 'hover:bg-accent'}`;

  return (
    <aside
      className="hidden md:flex md:flex-col fixed inset-y-0 left-0 w-60 border-r bg-background p-3 gap-1 z-30 print:hidden"
      aria-label={t('nav.panel')}
    >
      <div className="font-bold text-lg px-3 py-3">{t('appName')}</div>
      <NavLink to="/inicio" className={link} end>
        <Home className="h-5 w-5" aria-hidden />
        {t('nav.inicio')}
      </NavLink>
      <NavLink to="/animales" className={link}>
        <Beef className="h-5 w-5" aria-hidden />
        {t('nav.animales')}
      </NavLink>
      <NavLink to="/hacer-nota" className={link}>
        <PencilLine className="h-5 w-5" aria-hidden />
        {t('nav.hacerNota')}
      </NavLink>
      <NavLink to="/panel" className={link}>
        <LayoutGrid className="h-5 w-5" aria-hidden />
        {t('nav.panel')}
      </NavLink>
      <NavLink to="/ajustes" className={link}>
        <Settings className="h-5 w-5" aria-hidden />
        {t('nav.ajustes')}
      </NavLink>
    </aside>
  );
}
```

- [ ] **Paso 4 — crear `AppShell`**

Crear `frontend/src/components/app-shell.tsx`:
```tsx
import { Outlet } from 'react-router-dom';
import { UserMenu } from './user-menu';
import { LanguageSwitcher } from './language-switcher';
import { ThemeToggle } from './theme-toggle';
import { BottomNav } from './bottom-nav';
import { DesktopSidebar } from './desktop-sidebar';

/**
 * Shell autenticado: header + sidebar lateral en escritorio, bottom nav en movil.
 * El main tiene padding inferior en movil para no quedar oculto por el bottom nav,
 * y desplazamiento lateral en escritorio para hacer espacio a la sidebar.
 */
export function AppShell() {
  return (
    <div className="min-h-screen flex flex-col">
      <header className="border-b flex items-center justify-between px-4 py-2 print:hidden md:ml-60">
        <div className="font-bold md:hidden">Digital Cow</div>
        <div className="hidden md:block" />
        <div className="flex items-center gap-2">
          <LanguageSwitcher />
          <ThemeToggle />
          <UserMenu />
        </div>
      </header>
      <DesktopSidebar />
      <main className="flex-1 p-4 overflow-auto pb-24 md:pb-4 md:ml-60 print:p-0 print:ml-0">
        <Outlet />
      </main>
      <BottomNav />
    </div>
  );
}
```

- [ ] **Paso 5 — actualizar `AppLayout` para delegar en `AppShell`**

Editar `frontend/src/pages/AppLayout.tsx` por completo:
```tsx
import { AppShell } from '@/components/app-shell';

/** Shell autenticado: delega en AppShell (Fase 6). */
export default function AppLayout() {
  return <AppShell />;
}
```

- [ ] **Paso 6 — crear stubs de paginas Inicio, Panel, Hacer una nota, Ajustes**

Crear `frontend/src/pages/inicio/InicioPage.tsx`:
```tsx
import DashboardPage from '../dashboard/DashboardPage';

/** Pagina Inicio (Fase 6). En 6.6 se rediseña; por ahora reusa el dashboard actual. */
export default function InicioPage() {
  return <DashboardPage />;
}
```

Crear `frontend/src/pages/panel/PanelIndexPage.tsx`:
```tsx
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Activity, Wheat, DollarSign, Heart, Scale } from 'lucide-react';

const tiles = [
  { to: '/panel/salud', icon: Activity, key: 'nav.panelSalud' },
  { to: '/panel/alimentacion', icon: Wheat, key: 'nav.panelAlimentacion' },
  { to: '/panel/dinero', icon: DollarSign, key: 'nav.panelDinero' },
  { to: '/panel/reproduccion', icon: Heart, key: 'nav.panelReproduccion' },
  { to: '/panel/produccion', icon: Scale, key: 'nav.panelProduccion' }
] as const;

/** Hub del Panel: tarjetas a las sub-paginas (Fase 6, stub). */
export default function PanelIndexPage() {
  const { t } = useTranslation('common');
  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold">{t('nav.panel')}</h1>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        {tiles.map(tile => {
          const Icon = tile.icon;
          return (
            <Link
              key={tile.to}
              to={tile.to}
              className="flex items-center gap-4 rounded-xl border p-5 hover:bg-accent transition-colors min-h-24"
            >
              <Icon className="h-10 w-10 text-primary" aria-hidden />
              <span className="text-lg font-semibold">{t(tile.key)}</span>
            </Link>
          );
        })}
      </div>
    </div>
  );
}
```

Crear `frontend/src/pages/hacer-nota/HacerNotaPage.tsx`:
```tsx
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import {
  Syringe, Stethoscope, Pill, Scale, Milk, Wheat,
  ShoppingCart, Handshake, MinusCircle, PlusCircle, Heart, Sparkles
} from 'lucide-react';

const actions = [
  { to: '/health?tab=vaccinations', icon: Syringe, label: 'Vacune' },
  { to: '/health?tab=diagnoses', icon: Stethoscope, label: 'Diagnostique' },
  { to: '/health?tab=treatments', icon: Pill, label: 'Trate' },
  { to: '/production?tab=weighings', icon: Scale, label: 'Pese' },
  { to: '/production?tab=milkings', icon: Milk, label: 'Ordeñe' },
  { to: '/feeding?tab=records', icon: Wheat, label: 'Alimente' },
  { to: '/animals/new', icon: ShoppingCart, label: 'Compre animal' },
  { to: '/finance?tab=animal-sales', icon: Handshake, label: 'Vendi animal' },
  { to: '/finance?tab=expenses', icon: MinusCircle, label: 'Gaste' },
  { to: '/finance?tab=incomes', icon: PlusCircle, label: 'Recibi dinero' },
  { to: '/reproduction?tab=heats', icon: Heart, label: 'Vi un celo' },
  { to: '/reproduction?tab=pregnancy-checks', icon: Sparkles, label: 'Detecte preñez' }
] as const;

/**
 * Launcher "Hacer una nota" (stub Fase 6.1).
 * En 6.3 cada accion abrira un wizard propio en vez de redirigir al form existente.
 */
export default function HacerNotaPage() {
  const { t } = useTranslation('common');
  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold">{t('nav.hacerNota')}</h1>
      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-3">
        {actions.map(a => {
          const Icon = a.icon;
          return (
            <Link
              key={a.label}
              to={a.to}
              className="flex flex-col items-center justify-center gap-2 rounded-xl border p-5 hover:bg-accent transition-colors min-h-32 text-center"
            >
              <Icon className="h-10 w-10 text-primary" aria-hidden />
              <span className="text-base font-semibold">{a.label}</span>
            </Link>
          );
        })}
      </div>
    </div>
  );
}
```

Crear `frontend/src/pages/ajustes/AjustesIndexPage.tsx`:
```tsx
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { User, Building2, Map, Users, Tags, Pill, Palette } from 'lucide-react';

const tiles = [
  { to: '/settings/profile', icon: User, key: 'nav.ajustesPerfil' },
  { to: '/settings/account', icon: Building2, key: 'nav.ajustesCuenta' },
  { to: '/ranches', icon: Map, key: 'nav.ajustesRanchos' },
  { to: '/team', icon: Users, key: 'nav.ajustesEquipo' },
  { to: '/finance?tab=categories', icon: Tags, key: 'nav.ajustesCategorias' },
  { to: '/ajustes/medicamentos', icon: Pill, key: 'nav.ajustesMedicamentos' },
  { to: '/ajustes/idioma-tema', icon: Palette, key: 'nav.ajustesIdiomaTema' }
] as const;

/** Hub de Ajustes: tarjetas a sub-paginas existentes (Fase 6, stub). */
export default function AjustesIndexPage() {
  const { t } = useTranslation('common');
  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold">{t('nav.ajustes')}</h1>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        {tiles.map(tile => {
          const Icon = tile.icon;
          return (
            <Link
              key={tile.to}
              to={tile.to}
              className="flex items-center gap-4 rounded-xl border p-5 hover:bg-accent transition-colors min-h-24"
            >
              <Icon className="h-10 w-10 text-primary" aria-hidden />
              <span className="text-lg font-semibold">{t(tile.key)}</span>
            </Link>
          );
        })}
      </div>
    </div>
  );
}
```

Crear `frontend/src/pages/ajustes/AjustesIdiomaTemaPage.tsx`:
```tsx
import { useTranslation } from 'react-i18next';
import { LanguageSwitcher } from '@/components/language-switcher';
import { ThemeToggle } from '@/components/theme-toggle';

/** Pagina simple de seleccion de idioma y tema (Fase 6). */
export default function AjustesIdiomaTemaPage() {
  const { t } = useTranslation('common');
  return (
    <div className="space-y-6 max-w-md">
      <h1 className="text-2xl font-bold">{t('nav.ajustesIdiomaTema')}</h1>
      <section className="space-y-2">
        <h2 className="text-lg font-semibold">{t('language.label')}</h2>
        <LanguageSwitcher />
      </section>
      <section className="space-y-2">
        <h2 className="text-lg font-semibold">{t('themeToggle')}</h2>
        <ThemeToggle />
      </section>
    </div>
  );
}
```

(El stub `/ajustes/medicamentos` se crea como página vacía y se completa en iteración 6.4. Por ahora un placeholder amigable que indica "Disponible pronto" cumple).

Crear `frontend/src/pages/ajustes/AjustesMedicamentosPage.tsx`:
```tsx
import { useTranslation } from 'react-i18next';

/** Stub de catalogo de medicamentos (se implementa en 6.4). */
export default function AjustesMedicamentosPage() {
  const { t } = useTranslation('common');
  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold">{t('nav.ajustesMedicamentos')}</h1>
      <p className="text-muted-foreground">Disponible pronto.</p>
    </div>
  );
}
```

- [ ] **Paso 7 — actualizar router con nuevas rutas y redirects**

Editar `frontend/src/app/router.tsx`. Reemplazar el archivo completo:
```tsx
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from '@/pages/auth/LoginPage';
import RegisterPage from '@/pages/auth/RegisterPage';
import VerifyEmailPage from '@/pages/auth/VerifyEmailPage';
import ForgotPasswordPage from '@/pages/auth/ForgotPasswordPage';
import ResetPasswordPage from '@/pages/auth/ResetPasswordPage';
import AcceptInvitationPage from '@/pages/auth/AcceptInvitationPage';
import AppLayout from '@/pages/AppLayout';
import ProfileSettingsPage from '@/pages/settings/ProfileSettingsPage';
import AccountSettingsPage from '@/pages/settings/AccountSettingsPage';
import RanchesPage from '@/pages/ranches/RanchesPage';
import RanchDetailPage from '@/pages/ranches/RanchDetailPage';
import TeamPage from '@/pages/team/TeamPage';
import AnimalsListPage from '@/pages/animals/AnimalsListPage';
import AnimalDetailPage from '@/pages/animals/AnimalDetailPage';
import AnimalEditPage from '@/pages/animals/AnimalEditPage';
import AdminLoginPage from '@/pages/admin/AdminLoginPage';
import AdminAccountsPage from '@/pages/admin/AdminAccountsPage';
import HealthPanelPage from '@/pages/health/HealthPanelPage';
import HealthPlanDetailPage from '@/pages/health/HealthPlanDetailPage';
import ReproductionPanelPage from '@/pages/reproduction/ReproductionPanelPage';
import ProductionPanelPage from '@/pages/production/ProductionPanelPage';
import FeedingPanelPage from '@/pages/feeding/FeedingPanelPage';
import FeedingPlanDetailPage from '@/pages/feeding/FeedingPlanDetailPage';
import FinancePanelPage from '@/pages/finance/FinancePanelPage';
import ReportsPanelPage from '@/pages/reports/ReportsPanelPage';
import AnimalReportPage from '@/pages/reports/AnimalReportPage';
import InicioPage from '@/pages/inicio/InicioPage';
import PanelIndexPage from '@/pages/panel/PanelIndexPage';
import HacerNotaPage from '@/pages/hacer-nota/HacerNotaPage';
import AjustesIndexPage from '@/pages/ajustes/AjustesIndexPage';
import AjustesIdiomaTemaPage from '@/pages/ajustes/AjustesIdiomaTemaPage';
import AjustesMedicamentosPage from '@/pages/ajustes/AjustesMedicamentosPage';
import { ProtectedRoute } from '@/components/protected-route';

/** Router top-level (Fase 6: shell de cinco destinos + compatibilidad con rutas viejas). */
export function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/inicio" replace />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/verify-email" element={<VerifyEmailPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />
        <Route path="/accept-invitation" element={<AcceptInvitationPage />} />

        <Route path="/admin/login" element={<AdminLoginPage />} />
        <Route path="/admin/accounts" element={<AdminAccountsPage />} />

        <Route element={<ProtectedRoute><AppLayout /></ProtectedRoute>}>
          {/* Cinco destinos nuevos */}
          <Route path="/inicio" element={<InicioPage />} />
          <Route path="/animales" element={<AnimalsListPage />} />
          <Route path="/animales/nuevo" element={<AnimalEditPage />} />
          <Route path="/animales/:id" element={<AnimalDetailPage />} />
          <Route path="/animales/:id/editar" element={<AnimalEditPage />} />
          <Route path="/hacer-nota" element={<HacerNotaPage />} />

          {/* Panel y sus sub-paginas (siguen reusando las pages actuales internamente) */}
          <Route path="/panel" element={<PanelIndexPage />} />
          <Route path="/panel/salud" element={<HealthPanelPage />} />
          <Route path="/panel/salud/planes/:id" element={<HealthPlanDetailPage />} />
          <Route path="/panel/alimentacion" element={<FeedingPanelPage />} />
          <Route path="/panel/alimentacion/planes/:id" element={<FeedingPlanDetailPage />} />
          <Route path="/panel/dinero" element={<FinancePanelPage />} />
          <Route path="/panel/reproduccion" element={<ReproductionPanelPage />} />
          <Route path="/panel/produccion" element={<ProductionPanelPage />} />
          <Route path="/panel/reportes" element={<ReportsPanelPage />} />
          <Route path="/panel/reportes/animal/:id" element={<AnimalReportPage />} />

          {/* Ajustes */}
          <Route path="/ajustes" element={<AjustesIndexPage />} />
          <Route path="/ajustes/perfil" element={<ProfileSettingsPage />} />
          <Route path="/ajustes/cuenta" element={<AccountSettingsPage />} />
          <Route path="/ajustes/ranchos" element={<RanchesPage />} />
          <Route path="/ajustes/ranchos/:id" element={<RanchDetailPage />} />
          <Route path="/ajustes/equipo" element={<TeamPage />} />
          <Route path="/ajustes/medicamentos" element={<AjustesMedicamentosPage />} />
          <Route path="/ajustes/idioma-tema" element={<AjustesIdiomaTemaPage />} />

          {/* Rutas viejas: redirects a nuevas para no romper bookmarks */}
          <Route path="/dashboard" element={<Navigate to="/inicio" replace />} />
          <Route path="/animals" element={<Navigate to="/animales" replace />} />
          <Route path="/animals/new" element={<Navigate to="/animales/nuevo" replace />} />
          <Route path="/animals/:id" element={<Navigate to="/animales/:id" replace />} />
          <Route path="/animals/:id/edit" element={<Navigate to="/animales/:id/editar" replace />} />
          <Route path="/ranches" element={<Navigate to="/ajustes/ranchos" replace />} />
          <Route path="/ranches/:id" element={<RanchDetailPage />} />
          <Route path="/team" element={<Navigate to="/ajustes/equipo" replace />} />
          <Route path="/settings/profile" element={<Navigate to="/ajustes/perfil" replace />} />
          <Route path="/settings/account" element={<Navigate to="/ajustes/cuenta" replace />} />
          <Route path="/health" element={<Navigate to="/panel/salud" replace />} />
          <Route path="/health/vaccinations" element={<Navigate to="/panel/salud?tab=vaccinations" replace />} />
          <Route path="/health/diagnoses" element={<Navigate to="/panel/salud?tab=diagnoses" replace />} />
          <Route path="/health/treatments" element={<Navigate to="/panel/salud?tab=treatments" replace />} />
          <Route path="/health/pest-controls" element={<Navigate to="/panel/salud?tab=pest-controls" replace />} />
          <Route path="/health/vet-visits" element={<Navigate to="/panel/salud?tab=vet-visits" replace />} />
          <Route path="/health/plans" element={<Navigate to="/panel/salud?tab=plans" replace />} />
          <Route path="/health/plans/:id" element={<HealthPlanDetailPage />} />
          <Route path="/reproduction" element={<Navigate to="/panel/reproduccion" replace />} />
          <Route path="/reproduction/bulls" element={<Navigate to="/panel/reproduccion?tab=bulls" replace />} />
          <Route path="/reproduction/semen" element={<Navigate to="/panel/reproduccion?tab=semen" replace />} />
          <Route path="/reproduction/heats" element={<Navigate to="/panel/reproduccion?tab=heats" replace />} />
          <Route path="/reproduction/services" element={<Navigate to="/panel/reproduccion?tab=services" replace />} />
          <Route path="/reproduction/pregnancy-checks" element={<Navigate to="/panel/reproduccion?tab=pregnancy-checks" replace />} />
          <Route path="/reproduction/calvings" element={<Navigate to="/panel/reproduccion?tab=calvings" replace />} />
          <Route path="/reproduction/abortions" element={<Navigate to="/panel/reproduccion?tab=abortions" replace />} />
          <Route path="/reproduction/weanings" element={<Navigate to="/panel/reproduccion?tab=weanings" replace />} />
          <Route path="/reproduction/dry-offs" element={<Navigate to="/panel/reproduccion?tab=dry-offs" replace />} />
          <Route path="/reproduction/kpis" element={<Navigate to="/panel/reproduccion?tab=kpis" replace />} />
          <Route path="/production" element={<Navigate to="/panel/produccion" replace />} />
          <Route path="/production/weighings" element={<Navigate to="/panel/produccion?tab=weighings" replace />} />
          <Route path="/production/milkings" element={<Navigate to="/panel/produccion?tab=milkings" replace />} />
          <Route path="/production/milk-samples" element={<Navigate to="/panel/produccion?tab=milk-samples" replace />} />
          <Route path="/production/bulk-tank" element={<Navigate to="/panel/produccion?tab=bulk-tank" replace />} />
          <Route path="/production/slaughter" element={<Navigate to="/panel/produccion?tab=slaughter" replace />} />
          <Route path="/production/growth-curve" element={<Navigate to="/panel/produccion?tab=growth-curve" replace />} />
          <Route path="/production/lactation-curve" element={<Navigate to="/panel/produccion?tab=lactation-curve" replace />} />
          <Route path="/production/kpis" element={<Navigate to="/panel/produccion?tab=kpis" replace />} />
          <Route path="/feeding" element={<Navigate to="/panel/alimentacion" replace />} />
          <Route path="/feeding/items" element={<Navigate to="/panel/alimentacion?tab=items" replace />} />
          <Route path="/feeding/plans" element={<Navigate to="/panel/alimentacion?tab=plans" replace />} />
          <Route path="/feeding/plans/:id" element={<FeedingPlanDetailPage />} />
          <Route path="/feeding/records" element={<Navigate to="/panel/alimentacion?tab=records" replace />} />
          <Route path="/feeding/cost-summary" element={<Navigate to="/panel/alimentacion?tab=cost-summary" replace />} />
          <Route path="/finance" element={<Navigate to="/panel/dinero" replace />} />
          <Route path="/finances" element={<Navigate to="/panel/dinero" replace />} />
          <Route path="/finances/expenses" element={<Navigate to="/panel/dinero?tab=expenses" replace />} />
          <Route path="/finances/incomes" element={<Navigate to="/panel/dinero?tab=incomes" replace />} />
          <Route path="/finances/animal-sales" element={<Navigate to="/panel/dinero?tab=animal-sales" replace />} />
          <Route path="/finances/milk-sales" element={<Navigate to="/panel/dinero?tab=milk-sales" replace />} />
          <Route path="/finances/categories" element={<Navigate to="/panel/dinero?tab=categories" replace />} />
          <Route path="/reports" element={<Navigate to="/panel/reportes" replace />} />
          <Route path="/reports/pnl" element={<Navigate to="/panel/reportes?tab=pnl" replace />} />
          <Route path="/reports/inventory" element={<Navigate to="/panel/reportes?tab=inventory" replace />} />
          <Route path="/reports/animal/:id" element={<AnimalReportPage />} />
          <Route path="/reports/sales-history" element={<Navigate to="/panel/reportes?tab=sales-history" replace />} />
          <Route path="/reports/health-summary" element={<Navigate to="/panel/reportes?tab=health-summary" replace />} />
        </Route>

        <Route path="*" element={<div className="p-8">404</div>} />
      </Routes>
    </BrowserRouter>
  );
}
```

- [ ] **Paso 8 — eliminar la sidebar antigua si no la referencia nadie**

Buscar referencias: `grep -rn "from.*components/sidebar" frontend/src`. Si la unica referencia era desde `AppLayout` (ya cambiado), eliminar `frontend/src/components/sidebar.tsx`. Si hay tests u otros usos, mantenerlo como wrapper que reexporte `DesktopSidebar` con la API minima.

- [ ] **Paso 9 — typecheck**

```
cd frontend && npm run typecheck
```
Esperado: cero errores.

- [ ] **Paso 10 — sanity manual (sin dev server, opcional)**

Iniciar `cd frontend && npm run dev` y verificar manualmente:
- `/` redirige a `/inicio`
- En escritorio: sidebar lateral con cinco enlaces
- En movil (DevTools, viewport iPhone): bottom nav con cinco destinos, FAB central
- Click en `/dashboard` redirige a `/inicio`
- Click en `/health` redirige a `/panel/salud`
- Click en `/animals` redirige a `/animales`

---

## Iteracion 6.2 — Componentes base accesibles

**Goal:** crear los building blocks reutilizables que las iteraciones posteriores consumen.

**Componentes a crear** en `frontend/src/components/ui/`:
- `helpful-field.tsx` — etiqueta con icono, texto de ayuda, ejemplo, boton "escuchar"
- `big-button.tsx` — variante de `button` con tamaño minimo 56px y label obligatorio
- `big-picker.tsx` — grid de tarjetas para selectores cortos
- `animal-avatar.tsx` — foto circular con fallback a iniciales sobre color de marca
- `icon-card.tsx` — tarjeta con icono grande, titulo y subtitulo
- `wizard-step.tsx` — encabezado "Paso N de M" + slot + Atras/Siguiente
- `speak-button.tsx` — boton mini que dispara `speechSynthesis.speak()` con el texto adyacente
- `empty-state.tsx` — placeholder amigable con icono, titulo, CTA
- `kpi-card.tsx` — numero grande + label + opcional delta

**Tests** Vitest + Testing Library para cada uno: render basico, accesibilidad (aria), interaccion de teclado.

**Hook** `frontend/src/lib/use-voice.ts` que expone `speak(text)` y respeta `useSettings().voiceEnabled` (preferencia en `localStorage` por ahora).

**Criterio de cierre:** los nueve componentes existen, tests verdes, axe-core no reporta violaciones en sus stories.

---

## Iteracion 6.3 — Launcher real "Hacer una nota" + wizards

**Goal:** reemplazar los redirects del stub por wizards reales que llamen a los endpoints existentes.

**Archivos a crear:**
- `frontend/src/features/wizard/WizardShell.tsx` — orquestador de pasos
- `frontend/src/features/wizard/steps/AnimalPickerStep.tsx` — selector con thumbnails
- `frontend/src/features/wizard/steps/ConfirmStep.tsx` — resumen + guardar
- `frontend/src/features/wizard/flows/VacunarFlow.tsx`
- `frontend/src/features/wizard/flows/DiagnosticarFlow.tsx`
- `frontend/src/features/wizard/flows/TratarFlow.tsx`
- `frontend/src/features/wizard/flows/PesarFlow.tsx`
- `frontend/src/features/wizard/flows/OrdenarFlow.tsx`
- `frontend/src/features/wizard/flows/AlimentarFlow.tsx`
- `frontend/src/features/wizard/flows/GastarFlow.tsx`
- `frontend/src/features/wizard/flows/RecibirDineroFlow.tsx`
- `frontend/src/features/wizard/flows/CeloFlow.tsx`
- `frontend/src/features/wizard/flows/PreniezFlow.tsx`
- `frontend/public/locales/{es,en}/wizard.json`

**Modificar:** `frontend/src/pages/hacer-nota/HacerNotaPage.tsx` para usar `WizardShell` en vez de enlaces externos. Cada accion abre un `<WizardShell flow={...} />` en una ruta `/hacer-nota/:flow`.

**Compra y venta de animales se aplazan a 6.5** (necesitan endpoint nuevo).

**Criterio de cierre:** las diez acciones funcionan en wizards de 3 a 4 pasos; el flujo guarda via los endpoints existentes; toast de confirmacion al cerrar.

---

## Iteracion 6.4 — Catalogo de medicamentos + escaneo de codigo de barras

**Goal:** entidad nueva `MedicineCatalog`, CRUD, lookup por barcode, escaneo en wizards de Vacune y Trate.

**Backend:**
- Migracion `V14__medicine_catalog.sql` (tabla + indices + seed bilingue con 30-50 medicamentos comunes en Latinoamerica).
- Migracion `V15__health_medicine_link.sql` (FK `medicine_id` en `vaccinations` y `treatments`).
- Modulo `com.digitalcow.health.medicine` (entity, repository, service, controller, dto, mapper).
- Endpoints: `GET /api/medicines`, `GET /api/medicines/{id}`, `GET /api/medicines/by-barcode/{barcode}`, `POST /api/medicines`, `PUT /api/medicines/{id}`, `DELETE /api/medicines/{id}`.
- Tests Testcontainers para barcode lookup (200 y 404).

**Frontend:**
- `npm i @zxing/browser @zxing/library` en `frontend/`.
- `frontend/src/components/barcode-scanner.tsx` — modal con preview de video y target visual; emite el codigo escaneado; gracefully degrada a input manual si no hay camara.
- `frontend/src/features/medicines/api.ts`, `types.ts`, `schemas.ts`.
- `frontend/src/features/medicines/components/MedicineForm.tsx` (CRUD).
- `frontend/src/features/medicines/components/MedicineSearchInput.tsx` (input + boton camara + autocomplete por texto).
- `frontend/src/pages/ajustes/AjustesMedicamentosPage.tsx` — listado + CRUD reemplazando el stub.
- Reemplazar selector libre de "medicamento" en formularios de Vacune y Trate por `MedicineSearchInput`.
- Tests Vitest del scanner (mock de `MediaDevices.getUserMedia`) y del lookup.

**Criterio de cierre:** se puede escanear un EAN-13 con la camara del laptop o del telefono y se pre-rellena el formulario.

---

## Iteracion 6.5 — Compra y venta integradas

**Goal:** alta de animal con compra atomica; baja con venta atomica.

**Backend:**
- Modulo `com.digitalcow.animal.purchase` con endpoint `POST /api/animals/with-purchase` que en una unica transaccion crea el animal y el `ExpenseEntity`.
- Tests Testcontainers que verifican rollback si la creacion del expense falla.
- Endpoint `POST /api/animals/{id}/sell-with-income` que pasa el animal a `SOLD` y crea un `IncomeEntity` con animalId asociado. Tests de rollback.

**Frontend:**
- `frontend/src/features/wizard/flows/ComprarAnimalFlow.tsx` (cuatro pasos: foto, identidad, origen y compra, confirmacion).
- `frontend/src/features/wizard/flows/VenderAnimalFlow.tsx` (tres pasos).
- Boton "Vender" en `AnimalDetailPage`.
- Reemplazar `/animales/nuevo` por el wizard de compra.

**Criterio de cierre:** se puede comprar un animal y aparece simultaneamente el gasto en Dinero; se puede vender y aparece simultaneamente el ingreso.

---

## Iteracion 6.6 — Reescritura de paginas Inicio, Salud, Alimentacion, Dinero

**Goal:** vista unica compuesta por panel (KPIs grandes, una grafica principal, secciones colapsables, "Ver todo" para tablas profundas).

**Archivos:** reescribir `InicioPage.tsx`, `HealthPanelPage.tsx`, `FeedingPanelPage.tsx`, `FinancePanelPage.tsx` para que NO usen `<Tabs>`. Las subpaginas (`VaccinationsPage`, `ExpensesPage`, etc.) quedan accesibles como rutas `/panel/salud/vacunas`, `/panel/dinero/gastos`, etc., enlazadas desde el botón "Ver todo" de cada seccion.

**Criterio de cierre:** ningun panel principal tiene mas de un nivel de tabs visible; toda la informacion sigue siendo accesible.

---

## Iteracion 6.7 — Detalle del animal con foto principal grande + linea de tiempo

**Goal:** `AnimalDetailPage` con hero 16:9 (foto principal), carrusel de fotos, tabs internos cortos (General, Salud, Alimentacion, Produccion, Reproduccion, Finanzas, Linea de tiempo).

**Archivos:**
- Modificar `frontend/src/pages/animals/AnimalDetailPage.tsx`.
- Crear `frontend/src/features/animals/components/AnimalHero.tsx`.
- Crear `frontend/src/features/animals/components/AnimalTimeline.tsx`.
- Backend: endpoint `GET /api/animals/{id}/timeline` que agrega vacunas, pesajes, tratamientos, cambios de lote, ventas, gastos del animal ordenados por fecha desc, con paginacion.

**Criterio de cierre:** la foto principal es lo primero que se ve; los tabs internos son cortos y rapidos; la linea de tiempo carga.

---

## Iteracion 6.8 — ComparisonChart por animal

**Goal:** grafica multi-serie por animal con dos a cuatro series (peso, alimento, gasto, ingreso) configurable.

**Backend:**
- Endpoint `GET /api/animals/{id}/comparison?series=weight,feed,expense,income&from=...&to=...&granularity=month|week|day` que devuelve un payload normalizado para Recharts.
- Implementacion con queries agregadas (sin materialized views).
- Tests Testcontainers que validan forma del payload y consistencia.

**Frontend:**
- `frontend/src/components/comparison-chart.tsx` (Recharts LineChart o ComposedChart con multiple Y axes).
- Integracion en `AnimalDetailPage` y en `AnimalReportPage`.

**Criterio de cierre:** se ven 2-4 series simultaneas con toggles funcionales y tooltips legibles.

---

## Iteracion 6.9 — Tarjetas y Mapa de animales + geolocalizacion de ranchos

**Goal:** `/animales` con tres modos (Lista, Tarjetas, Mapa). Mapa usa Leaflet con tiles de OpenStreetMap.

**Backend:**
- Migracion `V16__ranch_geo.sql` (lat/lng en `ranches` si no existen).
- Endpoint `PATCH /api/ranches/{id}/location`.

**Frontend:**
- `npm i leaflet react-leaflet @types/leaflet` en `frontend/`.
- Componente `RanchMap` con pin por rancho (popover con conteo y CTA "Ver animales aqui").
- Toggle de vista en `AnimalsListPage`.
- En `RanchDetailPage`: pickup de coordenadas via click en mapa o input numerico.

**Criterio de cierre:** se pueden geolocalizar ranchos y verlos en el mapa; click en pin filtra animales por rancho.

---

## Iteracion 6.10 — Resend + secretos + auditoria de strings + a11y final

**Goal:** infraestructura y pulido.

**Backend:**
- Reemplazar `JavaMailSender` por `ResendMailClient` (HTTP a `https://api.resend.com/emails`).
- Configurar `RESEND_API_KEY`, `MAIL_FROM_DOMAIN` por variables de entorno.
- Resilience4j (retry + circuit breaker).
- Tests WireMock.
- Eliminar configuracion SMTP obsoleta.

**Configuracion:**
- `.env.example` versionado.
- README actualizado con inventario de variables y procedimiento de rotacion.
- `docker-compose.yml` actualizado.
- Validacion al arranque: la app falla rapido si falta una variable critica.

**Frontend:**
- Barrido manual de strings i18n: reemplazar abreviaciones por palabras completas en todas las claves visibles.
- Test de lint que falle si una clave i18n contiene patrones de abreviacion ("ej.", "etc.", "vs.", "kg" suelto, etc.).
- Tests con axe-core en al menos 5 paginas (Inicio, Animales, Panel/Salud, Hacer una nota, Detalle de animal).

**Criterio de cierre:** correos saliendo via Resend en staging, axe-core verde en las 5 paginas, lint de strings sin warnings.

---

## Despliegue

Cada iteracion es desplegable de forma independiente. La iteracion 6.1 es la unica que cambia visualmente la navegacion sin cambios funcionales. Recomendado mergear y desplegar cada iteracion antes de empezar la siguiente.
