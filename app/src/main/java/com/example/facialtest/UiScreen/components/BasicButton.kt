package com.example.facialtest.UiScreen.components

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.example.facialtest.R

@Composable
fun BasicButton(value: String, onClick: () -> (Unit), modifier: Modifier = Modifier) {
  Button(modifier = modifier
    .heightIn(),
    colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.black)),
    shape = RoundedCornerShape(5.dp),
    onClick = { onClick() }) {
    //BoldTextField(value = value, size = 12.sp, textAlign = TextAlign.Center)
  }
}
