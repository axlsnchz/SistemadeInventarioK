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
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Nfc
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
import mx.edu.utez.sistemadeinventario.data.local.entity.ProductoEntity
import mx.edu.utez.sistemadeinventario.data.remote.RetrofitClient
import mx.edu.utez.sistemadeinventario.ui.theme.CardDark
import mx.edu.utez.sistemadeinventario.ui.theme.CharcoalBase
import mx.edu.utez.sistemadeinventario.ui.theme.MediumGrey
import mx.edu.utez.sistemadeinventario.ui.theme.OffWhite
import mx.edu.utez.sistemadeinventario.ui.theme.PastelCyan
import mx.edu.utez.sistemadeinventario.ui.theme.SurfaceInput

@Composable
fun ProductoCard(
    producto: ProductoEntity,
    onClick: () -> Unit,
    shape: Shape = BentoCardShape,
    modifier: Modifier = Modifier
) {
    val imageUrl = RetrofitClient.getImageUrl(producto.fotoUrl)
    val stockColor = stockPastelColor(producto.stock, producto.stockMinimo)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = CardDark
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = producto.nombre,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceInput)
                )
            } else {
                PastelIconCircle(
                    icon = Icons.Rounded.Inventory2,
                    backgroundColor = PastelCyan,
                    contentDescription = producto.nombre,
                    size = 54.dp,
                    iconSize = 24.dp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = producto.nombre,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = OffWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                val meta = buildList {
                    producto.categoria?.takeIf { it.isNotBlank() }?.let { add(it) }
                    producto.sku?.takeIf { it.isNotBlank() }?.let { add("SKU $it") }
                }.joinToString(" · ")

                if (meta.isNotEmpty()) {
                    Text(
                        text = meta,
                        fontSize = 12.sp,
                        color = MediumGrey,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(stockColor.copy(alpha = 0.18f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (producto.stock <= 0) "Agotado" else "Stock ${producto.stock}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = stockColor
                        )
                    }
                    if (!producto.nfcId.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(PastelCyan.copy(alpha = 0.18f))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Nfc,
                                contentDescription = "Con NFC",
                                tint = PastelCyan,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = formatPrice(producto.precio),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = OffWhite
            )
        }
    }
}
