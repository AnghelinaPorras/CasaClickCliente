package com.casaclick.cliente.ui.navigation
            import androidx.compose.foundation.*
            import androidx.compose.foundation.layout.*
            import androidx.compose.foundation.lazy.LazyColumn
            import androidx.compose.foundation.lazy.items
            import androidx.compose.foundation.lazy.grid.*
            import androidx.compose.foundation.shape.RoundedCornerShape
            import androidx.compose.material.icons.Icons
            import androidx.compose.material.icons.filled.*
            import androidx.compose.material3.*
            import androidx.compose.runtime.*
            import androidx.compose.runtime.saveable.rememberSaveable
            import androidx.compose.ui.*
            import androidx.compose.ui.graphics.Color
            import androidx.compose.ui.text.font.FontWeight
            import androidx.compose.ui.text.input.*
            import androidx.compose.ui.unit.dp
            import androidx.compose.ui.layout.ContentScale
            import androidx.lifecycle.compose.collectAsStateWithLifecycle
            import com.casaclick.cliente.BuildConfig
            import com.casaclick.cliente.viewmodel.*
            import com.casaclick.cliente.model.*
            import com.casaclick.cliente.ui.components.*

        import androidx.navigation.compose.*
        import androidx.lifecycle.viewmodel.compose.viewModel
        import com.casaclick.cliente.ui.screens.*
        import kotlinx.coroutines.delay
        @OptIn(ExperimentalMaterial3Api::class)
        @Composable fun AppNavigation(factory: CasaClickFactory) {
            val auth: AuthViewModel=viewModel(factory=factory);val pvm: PropiedadViewModel=viewModel(factory=factory);val vvm: VisitaViewModel=viewModel(factory=factory);val svm: SyncViewModel=viewModel(factory=factory)
            val usuario by auth.usuario.collectAsStateWithLifecycle();val conectado by svm.conectado.collectAsStateWithLifecycle()
            var splash by rememberSaveable { mutableStateOf(true) }
            LaunchedEffect(Unit) { delay(650);splash=false }
            if(splash) { SplashScreen();return }
            if(usuario==null) { LoginScreen(auth);return }
            val nav=rememberNavController();val entry by nav.currentBackStackEntryAsState();val ruta=entry?.destination?.route ?: "home"
            val snack=remember { SnackbarHostState() }
            val mp by pvm.mensaje.collectAsStateWithLifecycle();val mv by vvm.mensaje.collectAsStateWithLifecycle();val ms by svm.mensaje.collectAsStateWithLifecycle()
            LaunchedEffect(mp) { if(mp.isNotBlank()) { snack.showSnackbar(mp);pvm.mensaje.value="" } }
            LaunchedEffect(mv) { if(mv.isNotBlank()) { snack.showSnackbar(mv);vvm.mensaje.value="" } }
            LaunchedEffect(ms) { if(ms.isNotBlank()) { snack.showSnackbar(ms);svm.mensaje.value="" } }
            val principal=ruta in listOf("home","lista","visitas","sync")
            fun navegar(destino: String) { nav.navigate(destino) { launchSingleTop=true } }
            Scaffold(snackbarHost={SnackbarHost(snack)},topBar={ TopAppBar(title={Text("CasaClick "+if(BuildConfig.ROL=="cliente") "· Cliente" else "· Asesor")},navigationIcon={if(!principal) IconButton(onClick={nav.popBackStack()}) { Icon(Icons.Default.ArrowBack,"Volver") }},actions={TextButton(onClick=auth::salir) { Text("Salir") }}) },bottomBar={if(principal) NavigationBar {
                listOf(Triple("home","Inicio",Icons.Default.Home),Triple("lista","Explorar",Icons.Default.Search),Triple("visitas","Visitas",Icons.Default.CalendarMonth),Triple("sync","Sync",Icons.Default.Sync)).forEach { (r,t,i) -> NavigationBarItem(selected=ruta==r,onClick={nav.navigate(r) { popUpTo("home");launchSingleTop=true }},icon={Icon(i,t)},label={Text(t)}) }
            }}) { padding ->
                Column(Modifier.padding(padding)) {
                    ConnectivityBanner(conectado)
                    NavHost(nav,startDestination="home",modifier=Modifier.weight(1f)) {
                        composable("home") { HomeScreen(usuario!!.nombre,pvm,vvm,svm,::navegar) }
                        composable("lista") { ListScreen(pvm) { navegar("detalle/$it") } }
                        composable("detalle/{id}") { DetailScreen(it.arguments?.getString("id")?.toLongOrNull() ?: 0,pvm,::navegar,{nav.popBackStack()}) }
                        composable("visitas") { VisitasScreen(vvm,pvm,::navegar) }
                        composable("sync") { SyncScreen(svm) }
                        if(BuildConfig.ROL=="administrador") composable("form/{id}") { FormScreen(it.arguments?.getString("id")?.toLongOrNull() ?: 0,pvm) { nav.popBackStack() } }
                        else composable("agenda/{propiedad}/{visita}") { AgendaScreen(it.arguments?.getString("propiedad")?.toLongOrNull() ?: 0,it.arguments?.getString("visita")?.toLongOrNull() ?: 0,vvm) { nav.popBackStack() } }
                    }
                }
            }
        }
