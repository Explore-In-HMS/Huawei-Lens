/*
 * Copyright 2020. Explore in HMS. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.hms.referenceapp.huaweilens.common.utils

import java.util.*

object GetRegionByCountry {

    private val countriesByRegion: Map<String, String> = hashMapOf(
        Pair("Africa", "IO,BF,BI,CV,CM,CF,TD,KM,CG,CD,CI,DJ,EG,GQ,ER,SZ,ET,TF,GA,GM,GH,GN,GW,KE,LS,LR,LY,MG,MW,ML,MR,MU,YT,MA,MZ,NA,NE,NG,RE,RW,SH,ST,SN,SC,SL,SO,ZA,SS,SD,TZ,TG,TN,UG,EH,ZM,ZW"),
        Pair("Americas", "AI,AG,AR,AW,BS,BB,BZ,BM,BO,BQ,BV,BR,CA,KY,CL,CO,CR,CU,CW,DM,DO,EC,SV,FK,GF,GL,GD,GP,GT,GY,HT,HN,JM,MQ,MX,MS,NI,PA,PY,PE,PR,BL,KN,LC,MF,PM,VC,SX,GS,SR,TT,TC,US,UY,VE,VG,VI"),
        Pair("Antarctica", "AQ"),
        Pair("Europe", "AX,AL,AD,AT,BY,BE,BA,BG,HR,CZ,DK,EE,FO,FI,FR,DE,GI,GR,GG,VA,HU,IS,IE,IM,IT,JE,LV,LI,LT,LU,MT,MD,MC,ME,NL,MK,NO,PL,PT,RO,RU,SM,RS,SK,SI,ES,SJ,SE,CH,UA,GB"),
        Pair("Asia", "AF,AM,AZ,BH,BD,BT,BN,KH,CN,CY,GE,HK,IN,ID,IR,IQ,IL,JP,JO,KZ,KP,KR,KW,KG,LA,LB,MO,MY,MV,MN,MM,NP,OM,PK,PS,PH,QA,SA,SG,LK,SY,TR,TW,TJ,TH,TL,TM,AE,UZ,VN,YE"),
        Pair("Oceania", "AS,AU,CX,CC,CK,FJ,PF,GU,HM,KI,MH,FM,NR,NC,NZ,NU,NF,MP,PW,PG,PN,WS,SB,TK,TO,TV,UM,VU,WF")
    )

    fun get(countryCode: String): String? {
        val region: Array<String>? = countriesByRegion.filterValues {
            it.contains(countryCode.toUpperCase(Locale.ROOT))
        }.keys.toTypedArray()
        return if(!region.isNullOrEmpty()) region.first() else "N/A"
    }


}