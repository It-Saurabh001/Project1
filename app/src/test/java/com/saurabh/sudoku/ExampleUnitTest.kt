package com.saurabh.sudoku

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.saurabh.sudoku.domain.model.Notes
import org.junit.Test
import org.junit.Assert.*

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testNotesSerialization() {
        val gson = Gson()
        val notesType = object : TypeToken<Notes>() {}.type

        // Test 1: Deserializing default empty JSON string
        val emptyJson = "{}"
        val notesFromEmpty = gson.fromJson<Notes>(emptyJson, notesType) ?: Notes()
        println("notesFromEmpty object: $notesFromEmpty")
        
        // Assert if notes array field is null
        assertNotNull("notes array should not be null", notesFromEmpty.notes)
        println("notesFromEmpty.notes class: ${notesFromEmpty.notes.javaClass.name}")
        println("notesFromEmpty.notes[0][0] class: ${notesFromEmpty.notes[0][0].javaClass.name}")

        // Check if emptyJson deserialization results in sharing the same set reference
        val setA = notesFromEmpty.notes[0][0]
        val setB = notesFromEmpty.notes[0][1]
        println("EmptyJson: setA class = ${setA.javaClass.name}")
        assertFalse("Sets in emptyJson should not be the same reference", setA === setB)

        // Test 2: Deserializing valid serialized Notes object
        val original = Notes()
        val json = gson.toJson(original)
        println("Serialized JSON: $json")
        val deserialized = gson.fromJson<Notes>(json, notesType)

        assertNotNull(deserialized)
        assertNotNull(deserialized.notes)
        println("deserialized.notes class: ${deserialized.notes.javaClass.name}")
        println("deserialized.notes[0] class: ${deserialized.notes[0].javaClass.name}")
        println("deserialized.notes[0][0] class: ${deserialized.notes[0][0].javaClass.name}")

        // Modify a cell's set and check if other cells are modified
        deserialized.notes[0][0].add(7)
        println("deserialized[0][0]: ${deserialized.notes[0][0]}")
        println("deserialized[0][1]: ${deserialized.notes[0][1]}")

        assertEquals(1, deserialized.notes[0][0].size)
        assertEquals(0, deserialized.notes[0][1].size)
        assertFalse("Sets should not be the same reference", deserialized.notes[0][0] === deserialized.notes[0][1])
    }
}