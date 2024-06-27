package com.example.permissionhandlingexample2

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PermissionCard(
    permission: Permission,
    isGranted: Boolean,
    isPermanentlyDeclined: Boolean,
    onRequest: () -> Unit,
    onGoToSettings: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(text = permission.name)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Is granted: $isGranted")
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Is perm. declined: $isPermanentlyDeclined")
        }
        if(!isGranted && !isPermanentlyDeclined) {
            Button(onClick = onRequest) {
                Text(text = "Grant permission")
            }
        } else if(isPermanentlyDeclined && onGoToSettings != null) {
            Button(onClick = onGoToSettings) {
                Text(text = "Go to settings")
            }
        }
    }
}