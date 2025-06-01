import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scriptglance.ui.theme.Black18
import com.scriptglance.ui.theme.White
import com.scriptglance.ui.theme.WhiteF3

@Composable
fun AuthCard(
    title: String?,
    content: @Composable ColumnScope.() -> Unit,
    footer: @Composable (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WhiteF3)
            .padding(15.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.widthIn(max = 400.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(vertical = 32.dp, horizontal = 24.dp)
                            .fillMaxWidth()
                    ) {
                        if (title != null) {
                            Text(
                                title,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                fontSize = 28.sp,
                                color = Black18,
                                modifier = Modifier.padding(bottom = 22.dp)
                            )
                        }
                        content()
                        if (footer != null) {
                            Spacer(Modifier.height(18.dp))
                            footer()
                        }
                    }
                }
            }
        }
    }
}
