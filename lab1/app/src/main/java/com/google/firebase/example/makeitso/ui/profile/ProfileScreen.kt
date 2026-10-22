package com.google.firebase.example.makeitso.ui.profile

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.example.makeitso.R
import com.google.firebase.example.makeitso.data.model.ErrorMessage
import com.google.firebase.example.makeitso.ui.shared.CenterTopAppBar
import com.google.firebase.example.makeitso.ui.shared.StandardButton
import com.google.firebase.example.makeitso.ui.theme.DarkBlue
import com.google.firebase.example.makeitso.ui.theme.DarkGrey
import com.google.firebase.example.makeitso.ui.theme.LightRed
import com.google.firebase.example.makeitso.ui.theme.MakeItSoTheme
import kotlinx.serialization.Serializable

@Serializable
object ProfileRoute

@Composable
fun ProfileScreen(
    showErrorSnackbar: (ErrorMessage) -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()

    ProfileScreenContent(
        profile = profile,
        loadProfile = viewModel::loadProfile,
        signOut = viewModel::signOut,
        createPasskey = viewModel::createPasskey,
        deleteAccount = viewModel::deleteAccount,
        showErrorSnackbar = showErrorSnackbar
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ProfileScreenContent(
    profile: ProfileInfo,
    loadProfile: () -> Unit,
    signOut: (Context, (ErrorMessage) -> Unit) -> Unit,
    createPasskey: (Context, (ErrorMessage) -> Unit) -> Unit,
    deleteAccount: (Context, (ErrorMessage) -> Unit) -> Unit,
    showErrorSnackbar: (ErrorMessage) -> Unit
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    LaunchedEffect(true) {
        loadProfile()
    }

    Scaffold(
        topBar = {
            CenterTopAppBar(
                title = stringResource(R.string.profile),
                scrollBehavior = scrollBehavior
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    start = 24.dp,
                    end = 24.dp,
                    bottom = 4.dp
                )
        ) {
            Spacer(Modifier.size(24.dp))

            ProfileField(R.string.sign_in_method, profile.method)
            ProfileField(R.string.email, profile.email)
            ProfileField(R.string.name, profile.name.ifBlank { "-" })
            ProfileField(R.string.email_verified, profile.emailVerified.toString())
            ProfileField(R.string.user_id, profile.uid)

            Spacer(Modifier.size(24.dp))

            StandardButton(
                label = R.string.create_passkey,
                onButtonClick = { createPasskey(context, showErrorSnackbar) }
            )

            Spacer(Modifier.size(16.dp))

            StandardButton(
                label = R.string.sign_out,
                onButtonClick = { signOut(context, showErrorSnackbar) }
            )

            Spacer(Modifier.size(16.dp))

            DeleteAccountButton { deleteAccount(context, showErrorSnackbar) }
        }
    }
}

@Composable
private fun ProfileField(label: Int, value: String) {
    Text(text = stringResource(label), fontSize = 12.sp, color = DarkGrey)
    Text(text = value, fontSize = 16.sp)
    Spacer(Modifier.size(12.dp))
}

@Composable
fun DeleteAccountButton(deleteAccount: () -> Unit) {
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    StandardButton(
        label = R.string.delete_account,
        onButtonClick = {
            showDeleteAccountDialog = true
        }
    )

    if (showDeleteAccountDialog) {
        AlertDialog(
            containerColor = LightRed,
            textContentColor = DarkBlue,
            titleContentColor = DarkBlue,
            title = { Text(stringResource(R.string.delete_account_title)) },
            text = { Text(stringResource(R.string.delete_account_description)) },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteAccountDialog = false },
                    colors = getDialogButtonColors()
                ) {
                    Text(text = stringResource(R.string.cancel), fontSize = 16.sp)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAccountDialog = false
                        deleteAccount()
                    },
                    colors = getDialogButtonColors()
                ) {
                    Text(text = stringResource(R.string.delete), fontSize = 16.sp)
                }
            },
            onDismissRequest = { showDeleteAccountDialog = false }
        )
    }
}

private fun getDialogButtonColors(): ButtonColors {
    return ButtonColors(
        containerColor = LightRed,
        contentColor = DarkBlue,
        disabledContainerColor = LightRed,
        disabledContentColor = DarkGrey
    )
}

@Composable
@Preview(showSystemUi = true)
fun ProfileScreenPreview() {
    MakeItSoTheme(darkTheme = true) {
        ProfileScreenContent(
            profile = ProfileInfo("Google", "user@example.com", "User", "uid", true),
            loadProfile = {},
            signOut = { _, _ -> },
            createPasskey = { _, _ -> },
            deleteAccount = { _, _ -> },
            showErrorSnackbar = {}
        )
    }
}
