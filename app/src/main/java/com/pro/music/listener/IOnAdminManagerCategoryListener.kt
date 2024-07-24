package com.pro.music.listener

import com.pro.music.model.Category

interface IOnAdminManagerCategoryListener {
    fun onClickUpdateCategory(category: Category)
    fun onClickDeleteCategory(category: Category)
    fun onClickDetailCategory(category: Category)
}