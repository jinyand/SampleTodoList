package com.example.sampletodolist.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Todo(
    // autoGenerate : null을 받으면 자동으로 ID값 할당
    @PrimaryKey(autoGenerate = true)
    var id : Int?,
    @ColumnInfo(name = "title")
    var title: String,
    @ColumnInfo(name = "description")
    var description: String
) {
    constructor() : this(null, "", "")
}