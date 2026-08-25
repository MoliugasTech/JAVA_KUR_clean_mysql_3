package com.example.fooddeliverymobile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fooddeliverymobile.data.AuthSession
import com.example.fooddeliverymobile.data.MobileRole

private val Forest = Color(0xFF12372A)
private val ForestLight = Color(0xFF2A6049)
private val Orange = Color(0xFFEB681F)
private val Cream = Color(0xFFF5F7F2)
private val Muted = Color(0xFF6B7972)

@Composable
fun FoodDeliveryMobileApp(loginViewModel: LoginViewModel = viewModel()) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Orange,
            secondary = ForestLight,
            background = Cream,
            surface = Color.White,
            onBackground = Forest,
            onSurface = Forest,
        ),
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Cream) {
            val state = loginViewModel.state
            if (state.session == null) {
                LoginScreen(
                    state = state,
                    onUsernameChange = loginViewModel::updateUsername,
                    onPasswordChange = loginViewModel::updatePassword,
                    onLogin = loginViewModel::login,
                )
            } else {
                RoleDashboard(session = state.session, onLogout = loginViewModel::logout)
            }
        }
    }
}

@Composable
private fun LoginScreen(
    state: LoginUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize().background(Forest).padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("FOOD DELIVERY", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 28.sp)
            Text("Klientams ir vairuotojams", color = Color(0xFFC6D6CE), fontSize = 16.sp)
            Spacer(Modifier.height(28.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text("Prisijungimas", color = Forest, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Text("Naudokite savo sistemos paskyrą", color = Muted)
                    OutlinedTextField(
                        value = state.username,
                        onValueChange = onUsernameChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Vartotojo vardas") },
                        singleLine = true,
                        enabled = !state.loading,
                    )
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = onPasswordChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Slaptažodis") },
                        singleLine = true,
                        enabled = !state.loading,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    )
                    state.error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                    }
                    Button(
                        onClick = onLogin,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = !state.loading,
                        colors = ButtonDefaults.buttonColors(containerColor = Orange),
                    ) {
                        if (state.loading) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Prisijungti", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleDashboard(session: AuthSession, onLogout: () -> Unit) {
    val client = session.role == MobileRole.CLIENT
    val title = if (client) "Kliento erdvė" else "Vairuotojo erdvė"
    val description = if (client) {
        "Restoranų, krepšelio ir užsakymų ekranai bus prijungti kitame etape."
    } else {
        "Laisvų pristatymų ir mano užsakymų ekranai bus prijungti kitame etape."
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Spacer(Modifier.height(20.dp))
        Text("FOOD DELIVERY", color = Orange, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Sveiki, ${session.username}", color = Forest, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text(title, color = ForestLight, fontSize = 20.sp)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
        ) {
            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Prisijungta", color = Muted)
                    Text(session.role.name, color = Forest, fontWeight = FontWeight.Bold)
                }
                Text(description, color = Muted)
            }
        }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Forest),
        ) {
            Text("Atsijungti", fontWeight = FontWeight.Bold)
        }
    }
}
