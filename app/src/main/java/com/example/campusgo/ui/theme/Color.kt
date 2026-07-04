package com.example.campusgo.ui.theme

import androidx.compose.ui.graphics.Color

// Azul institucional — cor de marca do CampusGo. Paleta completa (incluindo os "...Container",
// usados p.ex. no indicador da tab selecionada e em fundos de círculos) para não sobrar nenhuma
// cor da paleta base do Material3 por definir. Tertiary em âmbar, usado como cor do estado
// "Em análise" (ver corEstado em ListaPedidosScreen.kt) — contraste de propósito com o azul.
val BluePrimaryLight = Color(0xFF1565C0)
val BlueOnPrimaryLight = Color(0xFFFFFFFF)
val BluePrimaryContainerLight = Color(0xFFBBDEFB)
val BlueOnPrimaryContainerLight = Color(0xFF001D36)
val BlueSecondaryLight = Color(0xFF4F6072)
val BlueSecondaryContainerLight = Color(0xFFD3E4F5)
val BlueOnSecondaryContainerLight = Color(0xFF0C1D2A)
val AmberTertiaryLight = Color(0xFF8A5000)
val AmberTertiaryContainerLight = Color(0xFFFFDDB3)
val AmberOnTertiaryContainerLight = Color(0xFF2B1700)

val BluePrimaryDark = Color(0xFF90CAF9)
val BlueOnPrimaryDark = Color(0xFF003258)
val BluePrimaryContainerDark = Color(0xFF004881)
val BlueOnPrimaryContainerDark = Color(0xFFD1E4FF)
val BlueSecondaryDark = Color(0xFFB7CADE)
val BlueSecondaryContainerDark = Color(0xFF374758)
val BlueOnSecondaryContainerDark = Color(0xFFD3E4F5)
val AmberTertiaryDark = Color(0xFFFFB951)
val AmberTertiaryContainerDark = Color(0xFF5B3F00)
val AmberOnTertiaryContainerDark = Color(0xFFFFDDB3)

// Branco puro — fundo/superfície do tema claro, para combinar com o fundo branco do logótipo
// (ver campusgo_logo.png) em vez do branco levemente arroxeado da paleta base do Material3.
val BackgroundLight = Color(0xFFFFFFFF)
val OnBackgroundLight = Color(0xFF1A1C1E)

// Família completa de "surfaceContainer*" em cinza neutro — Card e NavigationBar usam estas
// cores por omissão (não `background`/`surface`), e sem as definir aqui continuavam a cair na
// paleta roxa base do Material3, mesmo depois de corrigir o resto.
val SurfaceVariantLight = Color(0xFFE7E9EC)
val OnSurfaceVariantLight = Color(0xFF44474A)
val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFF5F5F7)
val SurfaceContainerLight = Color(0xFFEFEFF2)
val SurfaceContainerHighLight = Color(0xFFE9E9EC)
val SurfaceContainerHighestLight = Color(0xFFE3E3E6)
val OutlineLight = Color(0xFF74777A)
val OutlineVariantLight = Color(0xFFC4C6C8)

val SurfaceDarkNeutral = Color(0xFF121316)
val OnSurfaceDarkNeutral = Color(0xFFE3E2E6)
val SurfaceVariantDark = Color(0xFF44474A)
val OnSurfaceVariantDark = Color(0xFFC4C6C8)
val SurfaceContainerLowestDark = Color(0xFF0D0E11)
val SurfaceContainerLowDark = Color(0xFF1A1B1E)
val SurfaceContainerDark = Color(0xFF1E1F22)
val SurfaceContainerHighDark = Color(0xFF28292C)
val SurfaceContainerHighestDark = Color(0xFF333437)
val OutlineDark = Color(0xFF8E9092)
val OutlineVariantDark = Color(0xFF44474A)
