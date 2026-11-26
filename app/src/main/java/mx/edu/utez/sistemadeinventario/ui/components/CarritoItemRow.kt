package mx.edu.utez.sistemadeinventario.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import mx.edu.utez.sistemadeinventario.data.local.entity.CarritoEntity
import mx.edu.utez.sistemadeinventario.data.remote.RetrofitClient
import mx.edu.utez.sistemadeinventario.ui.theme.ActiveWhite
import mx.edu.utez.sistemadeinventario.ui.theme.CardDark
import mx.edu.utez.sistemadeinventario.ui.theme.CharcoalBase
import mx.edu.utez.sistemadeinventario.ui.theme.MediumGrey
import mx.edu.utez.sistemadeinventario.ui.theme.OffWhite
import mx.edu.utez.sistemadeinventario.ui.theme.PastelMint
import mx.edu.utez.sistemadeinventario.ui.theme.PastelPink
import mx.edu.utez.sistemadeinventario.ui.theme.SurfaceInput
import mx.edu.utez.sistemadeinventario.ui.theme.SurfaceTonal

@Composable
fun CarritoItemRow(
    item: CarritoEntity,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    onRemove: () -> Unit,
    shape: Shape = BentoCardShape,
    modifier: Modifier = Modifier
) {
    val imageUrl = RetrofitClient.getImageUrl(item.fotoUrl)
    val subtotal = item.precioUnitario * item.cantidad

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = CardDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = item.nombre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceInput)
                    )
                } else {
                    PastelIconCircle(
                        icon = Icons.Rounded.Inventory2,
                        backgroundColor = PastelMint,
                        contentDescription = item.nombre,
                        size = 48.dp,
                        iconSize = 22.dp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.nombre,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = OffWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${formatPrice(item.precioUnitario)} c/u · Disp: ${item.stockDisponible}",
                        fontSize = 12.sp,
                        color = MediumGrey
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(SurfaceTonal)
                        .clickable(onClick = onRemove),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Quitar producto",
                        tint = PastelPink,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(SurfaceTonal)
                            .clickable(onClick = onDecrement),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Remove,
                            contentDescription = "Disminuir cantidad",
                            tint = OffWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = item.cantidad.toString(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OffWhite
                    )

                    val canAdd = item.cantidad < item.stockDisponible
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (canAdd) ActiveWhite else SurfaceTonal.copy(alpha = 0.5f))
                            .clickable(enabled = canAdd, onClick = onIncrement),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Aumentar cantidad",
                            tint = if (canAdd) CharcoalBase else MediumGrey,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = formatPrice(subtotal),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OffWhite
                )
            }
        }
    }
}
