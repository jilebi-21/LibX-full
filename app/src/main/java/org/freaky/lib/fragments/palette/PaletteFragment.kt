package org.freaky.lib.fragments.palette

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.annotation.AttrRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.libx.ui.utils.AutoGridLayoutManager
import org.freaky.lib.R

class PaletteFragment : Fragment(R.layout.fragment_palette) {

    data class PaletteItem(var color: Int, var name: String, var textColor: Int = Color.BLACK)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)

        recyclerView.adapter = PaletteAdapter(getPaletteList())
        recyclerView.layoutManager = AutoGridLayoutManager(context, resources.displayMetrics.density * 150f)
    }

    private fun getPaletteList(): List<PaletteItem> {
        return ArrayList<PaletteItem>().apply {
            val colorPrimary = getColor(R.attr.colorPrimary)
            val colorOnPrimary = getColor(R.attr.colorOnPrimary)
            val colorPrimaryContainer = getColor(R.attr.colorPrimaryContainer)
            val colorOnPrimaryContainer = getColor(R.attr.colorOnPrimaryContainer)
            add(PaletteItem(colorPrimary, "Primary", colorOnPrimary))
            add(PaletteItem(colorOnPrimary, "On Primary", colorPrimary))
            add(PaletteItem(colorPrimaryContainer, "Primary Container", colorOnPrimaryContainer))
            add(PaletteItem(colorOnPrimaryContainer, "On Primary Container", colorPrimaryContainer))

            val colorSecondary = getColor(R.attr.colorSecondary)
            val colorOnSecondary = getColor(R.attr.colorOnSecondary)
            val colorSecondaryContainer = getColor(R.attr.colorSecondaryContainer)
            val colorOnSecondaryContainer = getColor(R.attr.colorOnSecondaryContainer)
            add(PaletteItem(colorSecondary, "Secondary", colorOnSecondary))
            add(PaletteItem(colorOnSecondary, "On Secondary", colorSecondary))
            add(PaletteItem(colorSecondaryContainer, "Secondary Container", colorOnSecondaryContainer))
            add(PaletteItem(colorOnSecondaryContainer, "On Secondary Container", colorSecondaryContainer))

            val colorTertiary = getColor(R.attr.colorTertiary)
            val colorOnTertiary = getColor(R.attr.colorOnTertiary)
            val colorTertiaryContainer = getColor(R.attr.colorTertiaryContainer)
            val colorOnTertiaryContainer = getColor(R.attr.colorOnTertiaryContainer)
            add(PaletteItem(colorTertiary, "Tertiary", colorOnTertiary))
            add(PaletteItem(colorOnTertiary, "On Tertiary", colorTertiary))
            add(PaletteItem(colorTertiaryContainer, "Tertiary Container", colorOnTertiaryContainer))
            add(PaletteItem(colorOnTertiaryContainer, "On Tertiary Container", colorTertiaryContainer))

            val colorSurface = getColor(R.attr.colorSurface)
            val colorOnSurface = getColor(R.attr.colorOnSurface)
            val colorSurfaceVariant = getColor(R.attr.colorSurfaceVariant)
            val colorOnSurfaceVariant = getColor(R.attr.colorOnSurfaceVariant)
            add(PaletteItem(colorSurface, "Surface", colorOnSurface))
            add(PaletteItem(colorOnSurface, "On Surface", colorSurface))
            add(PaletteItem(colorSurfaceVariant, "Surface Variant", colorOnSurfaceVariant))
            add(PaletteItem(colorOnSurfaceVariant, "On Surface Variant", colorSurfaceVariant))

            val colorError = getColor(R.attr.colorError)
            val colorOnError = getColor(R.attr.colorOnError)
            val colorErrorContainer = getColor(R.attr.colorErrorContainer)
            val colorOnErrorContainer = getColor(R.attr.colorOnErrorContainer)
            add(PaletteItem(colorError, "Error", colorOnError))
            add(PaletteItem(colorOnError, "On Error", colorError))
            add(PaletteItem(colorErrorContainer, "Error Container", colorOnErrorContainer))
            add(PaletteItem(colorOnErrorContainer, "On Error Container", colorErrorContainer))

            val colorOutline = getColor(R.attr.colorOutline)
            add(PaletteItem(colorOutline, "Outline"))
        }
    }

    private fun getColor(@AttrRes attr: Int): Int {
        val typedValue = TypedValue()
        context?.theme?.resolveAttribute(attr, typedValue, true) ?: return 0
        return typedValue.data
    }
}