package com.example.eric.footballbook

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.util.FuelRouting
import com.github.kittinunf.result.Result
import java.io.File
import java.net.URLEncoder

class NetworkStuffs {

    constructor(data: DataCapsule) {
        infos = data
    }

    var infos: DataCapsule

    private var g_login_url = "http://www.jdty.gov.cn/JdSportBureau_new/Yikatong/HtmlHelper/NewLoginHandler.ashx"
    private var g_book_center_url = "http://www.jdty.gov.cn/JdSportBureau_new/Yikatong/NewBookingCenter.aspx"
    private var g_book_stadium_url = "http://www.jdty.gov.cn/JdSportBureau_new/Yikatong/NewBookingIndex.aspx"
    private var g_book_url = "http://www.jdty.gov.cn/JdSportBureau_new/Yikatong/BookingSheet.aspx"
    private var g_book_image_url = "http://www.jdty.gov.cn/JdSportBureau_new/Yikatong/GetImage.aspx"
    private var g_book_list_rec = "http://www.jdty.gov.cn/JdSportBureau_new/Yikatong/HtmlHelper/GetRecordsList.ashx"
    private var g_book_detail_rec = "http://www.jdty.gov.cn/JdSportBureau_new/Yikatong/BookingDetail.aspx"

    private var g_stadium_infos = mapOf (
        "迎园中学" to Pair("1841e8a7-b2b5-4196-9e24-f4a53552ef0a", "a3915c4a-6bdc-43e6-8931-5293843fc540"),
        "体育场" to Pair("e6aff278-d523-42fa-9566-6bad1bca37f2", "0e759218-3806-4118-9e32-503fd4b4c096"),
        "南翔" to Pair("666a18fd-ac6e-40a7-9322-e6b54f46cedb", "73a96bbe-4e4c-43af-af65-6b0fd2efee77")
    )

    ///////////////////////////////////////////////////////////////
    // login function
    fun login() {
        var user = infos.data.getValue("user")
        var pwd = infos.data.getValue("pwd")
        Fuel.post(g_login_url, listOf("CardNo" to user, "PassWord" to pwd)).responseString {
                _, response, result ->
            when(result) {
                is Result.Success -> {
                    var session_id = response.headers["Set-Cookie"].toString()
                        .split(';')[0]
                        .split('[')[1]
                    FuelManager.instance.baseHeaders = mapOf("Cookie" to session_id)
                    visitBookCenter()
                }
                is Result.Failure -> {
//                    val ex = result.getException()
                }
            }
        }
    }

    //
    private fun visitBookCenter() {
        Fuel.get(g_book_center_url)
            .responseString() {
            _, _, result ->
            when(result) {
                is Result.Failure -> {
                }
                is Result.Success -> {
                    var stadium = infos.data.getValue("stadium").toString()
                    var params = FootballUtils.analyzeViewForm(result.get())
                    params += Pair("StadiumID", g_stadium_infos.getValue(stadium).first)
                    params += Pair("CardType", "普通卡")
                    visitStadiumDetail(params)
                }
            }
        }
    }

    private fun visitStadiumDetail(params: Map<String, String>) {
        Fuel.post(g_book_stadium_url, params.toList())
            .responseString {
                _, _, result ->
                when(result) {
                    is Result.Failure -> {
                    }
                    is Result.Success -> {
                        var new_params = FootballUtils.analyzeViewForm(result.get())
                        var start = infos.data.getValue("year").toString() + "-" +
                                    infos.data.getValue("month").toString() + "-" +
                                    infos.data.getValue("day").toString() + " " +
                                    infos.data.getValue("start").toString() + ":00"
                        new_params += Pair("ApplyTime", start)
                        var stadium = infos.data.getValue("stadium").toString()
                        new_params += Pair("AreaID", g_stadium_infos.getValue(stadium).second)
                        confirmStartTime(new_params)
                    }
                }
            }
    }

    private fun confirmStartTime(params: Map<String, String>) {
        var body = queryFromParameters(params.toList())
        body = body.replace("+", "%20")
        Fuel.post(g_book_url)
            .body(body)
            .responseString { _, _, result ->
                when(result) {
                    is Result.Failure -> {
                    }
                    is Result.Success -> {
                        var new_params = FootballUtils.analyzeViewForm(result.get())
                        confirmVerifyCode()
                    }
                }
        }
    }

    private fun confirmVerifyCode() {
//        Fuel.download(g_book_image_url)
//            .destination {
//                _, _ ->
//                File.createTempFile("football_img", ".jpg")
//            }.response {
//                _, _, res ->
//                var ss = res.get()
//            }
        Fuel.get(g_book_image_url)
            .response {
                _, _, result ->
                when(result) {
                    is Result.Failure -> {
                    }
                    is Result.Success -> {
                        var dd = result.get()
                    }
                }
            }
    }

    private fun queryFromParameters(params: List<Pair<String, Any?>>?): String = params.orEmpty()
        .filterNot { it.second == null }
        .map { (key, value) ->  URLEncoder.encode(key, "UTF-8") to URLEncoder.encode("$value", "UTF-8") }
        .joinToString("&") { (key, value) -> "$key=$value" }
}
