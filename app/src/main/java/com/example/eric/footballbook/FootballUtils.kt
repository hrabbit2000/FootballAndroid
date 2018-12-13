package com.example.eric.footballbook


class FootballUtils {
    //static functions
    companion object {
        fun analyzeViewForm(text: String): Map<String, String> {
            var state = getStringValue(text, "\"__VIEWSTATE\" value=\"")
            return mapOf("__VIEWSTATE" to state)
        }

        private fun getStringValue(text: String, key: String): String {
            var regex = Regex(key + "[^\"]+")
            var res = regex.find(text)?.value
            if (res == null) {
                res = ""
            } else {
                res = res.split(key)[1]
            }
            return res
        }

    }
}