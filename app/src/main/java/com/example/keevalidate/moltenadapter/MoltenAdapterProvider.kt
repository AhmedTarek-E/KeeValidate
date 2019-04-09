package com.baianat.app.makfy.moltenadapter

import com.example.keevalidate.moltenadapter.KotlinMoltenAdapter

class MoltenAdapterProvider private constructor() {

    private val adapterMap = HashMap<String, Any>()

    companion object {
        val INSTANCE by lazy { MoltenAdapterProvider() }
    }

    fun <T : Any> getMoltenAdapter(@ResLayout layout: Int, @ResId resIds : IntArray,
                                   names : Array<String>?, modelClass: Class<T>): KotlinMoltenAdapter<T> {

        if (!adapterMap.containsKey(modelClass.name)) {
            adapterMap[modelClass.name] = KotlinMoltenAdapter<T>(layout, null, resIds, names)
        }

        @Suppress("UNCHECKED_CAST")
        return adapterMap[modelClass.name] as KotlinMoltenAdapter<T>
    }

    fun <T : Any> getMoltenAdapter(@ResLayout layout: Int, modelClass: Class<T>): KotlinMoltenAdapter<T> {

        if (!adapterMap.containsKey(modelClass.name)) {
            adapterMap[modelClass.name] = KotlinMoltenAdapter(layout, null, modelClass)
        }

        @Suppress("UNCHECKED_CAST")
        return adapterMap[modelClass.name] as KotlinMoltenAdapter<T>
    }

    fun <T : Any> getMoltenAdapter(modelClass: Class<T>) : KotlinMoltenAdapter<T>? {
        if (adapterMap.containsKey(modelClass.name)) {
            @Suppress("UNCHECKED_CAST")
            return adapterMap[modelClass.name] as KotlinMoltenAdapter<T>
        }
        return null
    }

}