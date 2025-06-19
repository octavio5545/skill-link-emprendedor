# 📋 **Documentación Completa - SkillLink Authentication System**

## 🏗️ **Arquitectura General**

SkillLink es una aplicación de autenticación moderna construida con **React + TypeScript + Vite**, diseñada para una plataforma de incubación de emprendedores. La aplicación implementa un sistema completo de autenticación con registro, login, recuperación de contraseña y gestión de perfiles de usuario.

### **🎯 Características Principales**
- ✅ **Autenticación completa** (Login, Registro, Recuperación de contraseña)
- ✅ **Gestión de perfiles** con roles e intereses
- ✅ **Validaciones en tiempo real** de contraseñas
- ✅ **Interfaz responsive** con animaciones suaves
- ✅ **Arquitectura modular** y escalable
- ✅ **TypeScript** para type safety
- ✅ **Tailwind CSS** para estilos modernos

---

## 📁 **Estructura del Proyecto**

```
src/
├── api/                    # Capa de comunicación con backend
│   └── auth.ts            # Funciones de API para autenticación
├── components/            # Componentes reutilizables
│   ├── AuthBackground.tsx # Fondo animado del formulario
│   ├── AuthForm.tsx       # Formulario principal de autenticación
│   ├── AuthInput.tsx      # Input reutilizable con iconos
│   ├── PasswordValidation.tsx # Validaciones de contraseña
│   ├── UnifiedModal.tsx   # Modal para selección de rol/intereses
│   └── UnifiedSelector.tsx # Selector de perfil
├── hooks/                 # Hooks personalizados
│   └── useAuthTransition.ts # Lógica de estado de autenticación
├── pages/                 # Páginas de la aplicación
│   └── ResetPasswordPage.tsx # Página de cambio de contraseña
├── types/                 # Definiciones de tipos TypeScript
│   └── auth.ts           # Tipos para autenticación
├── App.tsx               # Componente raíz
├── main.tsx              # Punto de entrada
└── index.css             # Estilos globales y animaciones
```

---

## 🔧 **Componentes Principales**

### **🎯 App.tsx** - *Componente Raíz*
**Propósito:** Orquestador principal de la aplicación

```typescript
// Responsabilidades:
- Renderiza el layout principal con fondo animado
- Integra AuthBackground y AuthForm
- Maneja el hook useAuthTransition
- Proporciona el contexto visual (gradientes, elementos flotantes)
```

**Características:**
- Fondo animado con elementos flotantes
- Layout responsive con breakpoints
- Contenedor principal con glassmorphism
- Altura dinámica según el modo de autenticación

---

### **🔐 AuthForm.tsx** - *Formulario Principal*
**Propósito:** Maneja toda la lógica del formulario de autenticación

```typescript
// Responsabilidades:
- Renderiza campos según el modo (login/register/forgot)
- Maneja transiciones suaves entre modos
- Integra validaciones de contraseña
- Controla el estado de envío y mensajes de error
- Gestiona la visibilidad condicional de elementos
- Valida contraseña en tiempo real para habilitar/deshabilitar botón
```

**Estados del formulario:**
- **Login:** Email + Contraseña
- **Registro:** Nombre + Apellido + Email + Contraseña + Rol + Intereses
- **Recuperación:** Solo Email

---

### **📝 AuthInput.tsx** - *Input Reutilizable*
**Propósito:** Componente base para todos los campos de entrada

```typescript
// Responsabilidades:
- Renderiza input con icono y estilos consistentes
- Maneja estados de focus y hover
- Soporte para elementos adicionales (ej: botón mostrar/ocultar)
- Estilos adaptativos según el modo de autenticación
```

**Características:**
- Iconos dinámicos con Lucide React
- Efectos glassmorphism
- Colores adaptativos (púrpura para login, cyan para registro)
- Soporte para elementos del lado derecho

---

### **🎨 AuthBackground.tsx** - *Fondo Animado*
**Propósito:** Maneja toda la presentación visual del lado izquierdo

```typescript
// Responsabilidades:
- Gradientes animados según el modo
- Elementos decorativos (blobs, formas orgánicas)
- Transiciones suaves de colores y contenido
- Información promocional contextual
- Animaciones de entrada/salida
```

**Gradientes por modo:**
- **Login:** Púrpura → Azul → Índigo
- **Registro:** Esmeralda → Teal → Cyan

---

### **⚙️ UnifiedSelector.tsx** - *Selector de Rol e Intereses*
**Propósito:** Botón que abre el modal de selección

```typescript
// Responsabilidades:
- Muestra resumen de selecciones actuales
- Abre/cierra el modal de configuración
- Renderiza iconos dinámicos según selecciones
- Maneja estados vacíos vs. con datos
```

**Estados visuales:**
- **Vacío:** "Configurar perfil" con icono Settings
- **Con datos:** "Mentor • 5 intereses" con iconos específicos

---

### **🔧 UnifiedModal.tsx** - *Modal de Configuración*
**Propósito:** Interfaz completa para seleccionar rol e intereses

```typescript
// Responsabilidades:
- Navegación por pasos (rol → intereses)
- Renderiza grids de opciones con iconos
- Maneja selecciones múltiples para intereses
- Estados de validación y confirmación
- Animaciones de transición entre pasos
- Auto-navegación inteligente (si hay rol, va a intereses)
```

**Flujo de navegación:**
1. **Paso 1:** Selección de rol (Mentor/Colaborador)
2. **Paso 2:** Selección de intereses (múltiple)
3. **Confirmación:** Botón habilitado solo con datos válidos

---

### **✅ PasswordValidation.tsx** - *Validaciones de Contraseña*
**Propósito:** Muestra validaciones en tiempo real

```typescript
// Responsabilidades:
- 3 validaciones principales (longitud, dígito, carácter especial)
- Indicadores visuales (check/x) con colores
- Solo visible en modo registro
- Layout horizontal con espaciado equitativo
- Feedback visual inmediato al usuario
```

**Validaciones implementadas:**
- ✅ Mínimo 8 caracteres
- ✅ Al menos un dígito
- ✅ Al menos un carácter especial

---

### **🔄 ResetPasswordPage.tsx** - *Página de Recuperación*
**Propósito:** Maneja el cambio de contraseña con token

```typescript
// Responsabilidades:
- Validación de token de recuperación
- Formulario de nueva contraseña
- Confirmación de contraseña
- Estados de carga y error
- Navegación de regreso al login
```

**Estados de la página:**
- **Loading:** Validando token
- **Valid:** Formulario activo
- **Invalid:** Token expirado/inválido
- **Success:** Contraseña cambiada exitosamente
- **Error:** Error en el proceso

---

## 🧠 **Lógica y Estado**

### **🎣 useAuthTransition.ts** - *Hook Personalizado*
**Propósito:** Centraliza toda la lógica de estado de autenticación

```typescript
// Responsabilidades:
- Estado del formulario (formData, authMode, transiciones)
- Handlers para inputs, cambios de rol/intereses
- Lógica de envío y validación
- Comunicación con API
- Gestión de mensajes de error/éxito
- Control de estados de carga
```

**Estados manejados:**
- `authMode`: 'login' | 'register' | 'forgot'
- `formData`: Datos del formulario
- `isTransitioning`: Control de animaciones
- `showPassword`: Visibilidad de contraseña
- `apiMessage`: Mensajes de respuesta
- `isError`: Estado de error
- `isLoading`: Estado de carga

---

### **🌐 api/auth.ts** - *Capa de API*
**Propósito:** Maneja todas las comunicaciones con el backend

```typescript
// Responsabilidades:
- Funciones de registro, login, recuperación
- Manejo de errores HTTP
- Transformación de datos
- Configuración de headers y requests
- Gestión de tokens JWT
- Almacenamiento en sessionStorage
```

**Funciones principales:**
- `registerUser()`: Registro de nuevos usuarios
- `loginUser()`: Autenticación de usuarios
- `forgotPassword()`: Solicitud de recuperación
- `validateResetToken()`: Validación de token
- `resetPassword()`: Cambio de contraseña
- `fetchAuthenticated()`: Peticiones autenticadas
- `logoutUser()`: Limpieza de sesión

---

### **📋 types/auth.ts** - *Definiciones de Tipos*
**Propósito:** Centraliza todos los tipos TypeScript

```typescript
// Responsabilidades:
- Tipos para formularios (FormData, AuthMode)
- Enums para roles e intereses
- Interfaces para API requests/responses
- Tipos para props de componentes
```

**Tipos principales:**
- `AuthMode`: Modos de autenticación
- `UserRole`: Roles de usuario (mentor/colaborador)
- `UserInterest`: 19 categorías de intereses
- `FormData`: Estructura del formulario
- `RegisterRequest/Response`: API de registro
- `LoginRequest`: API de login
- `AuthResponse`: Respuesta de autenticación

---

## 🎨 **Estilos y Animaciones**

### **💫 index.css** - *Estilos Globales*
**Propósito:** Animaciones y utilidades CSS personalizadas

```css
/* Responsabilidades: */
- Keyframes para animaciones suaves
- Clases de utilidad para transiciones
- Efectos glassmorphism
- Responsive design helpers
- Scrollbar personalizado
- Animaciones de entrada/salida para texto
- Efectos blob y elementos flotantes
```

**Animaciones implementadas:**
- `spin-slow`: Rotación lenta para elementos decorativos
- `blob`: Movimiento orgánico de formas
- `fadeOutDown/fadeInUp`: Transiciones de texto
- `slideOutDown/slideInUp`: Transiciones de títulos

**Clases de utilidad:**
- `.smooth-transition`: Transiciones de 1200ms
- `.smooth-container`: Altura dinámica suave
- `.smooth-background`: Cambios de fondo suaves
- `.smooth-height`: Altura con overflow hidden

---

## 🔄 **Flujo de Datos**

### **📊 Flujo Principal**
```
App.tsx
├── useAuthTransition() → Estado global
├── AuthBackground → Presentación visual
└── AuthForm
    ├── AuthInput (múltiples) → Campos de entrada
    ├── UnifiedSelector → Botón de configuración
    │   └── UnifiedModal → Selección de rol/intereses
    └── PasswordValidation → Validaciones en tiempo real
```

### **🔄 Comunicación entre Componentes**
- **Props Down:** Datos fluyen desde App hacia componentes hijos
- **Events Up:** Eventos suben desde componentes hacia el hook
- **Estado Centralizado:** useAuthTransition maneja todo el estado
- **API Separada:** Comunicación con backend aislada

---

## 🚀 **Configuración y Dependencias**

### **📦 Dependencias Principales**
```json
{
  "react": "^19.1.0",
  "react-dom": "^19.1.0",
  "react-router-dom": "^7.6.2",
  "typescript": "~5.8.3",
  "tailwindcss": "^4.1.10",
  "lucide-react": "^0.515.0"
}
```


## 🎯 **Características Técnicas**

### **🔒 Seguridad**
- Validación de contraseñas en tiempo real
- Sanitización de inputs
- Manejo seguro de tokens JWT
- Almacenamiento en sessionStorage (no localStorage)
- Validación de tokens de recuperación

### **📱 Responsive Design**
- Mobile-first approach
- Breakpoints optimizados
- Layout adaptativo (sidebar oculto en móvil)
- Grids responsivos para intereses
- Touch-friendly interactions

### **⚡ Performance**
- Componentes optimizados con React.memo potencial
- Lazy loading preparado
- CSS optimizado sin duplicación
- Animaciones con GPU acceleration
- Debounce en validaciones

### **♿ Accesibilidad**
- Semantic HTML
- ARIA labels donde necesario
- Focus management
- Keyboard navigation
- Color contrast optimizado
- Reduced motion support

---

## 🎨 **Patrones de Diseño Implementados**

### **🏗️ Container/Presentational**
- **Container:** App.tsx, useAuthTransition
- **Presentational:** AuthForm, AuthInput, AuthBackground

### **🔧 Single Responsibility Principle**
- Cada componente tiene una función específica
- Separación clara entre lógica y presentación

### **🔄 Observer Pattern**
- Hook centralizado observa cambios de estado
- Componentes reaccionan a cambios automáticamente

### **🏭 Factory Pattern**
- AuthInput se configura según necesidades
- Modal genera opciones dinámicamente

---

