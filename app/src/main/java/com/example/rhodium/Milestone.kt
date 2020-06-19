package com.example.rhodium


//TODO add CINR
class Milestone() {
    var location: String? = null
    var technology: String? = null
    var signalStrength: String? = null
    var rxLev: String? = null
    var c1: String? = null
    var c2: String? = null
    var rscp: String? = null
    var rsrp: String? = null
    var rsrq: String? = null
    var ecno: String? = null
    var cinr: String? = null
    var tac: String? = null
    var lac: String? = null
    var plmn: String? = null
    var cellID: String? = null
    var color: String? = null
    var id: Int? = null


    constructor(
        location: String,
        tech: String,
        strength: String,
        c1: String,
        c2: String,
        rscp: String,
        rsrp: String,
        rsrq: String,
        ecno: String,
        cinr: String,
        tac: String,
        lac: String,
        plmn: String,
        id: Int,
        cellID: String,
        color: String,
        rxLev: String
    ) : this() {

        this.location = location
        this.technology = tech
        this.signalStrength = strength
        this.c1 = c1
        this.c2 = c2
        this.rscp = rscp
        this.rsrp = rsrp
        this.rsrq = rsrq
        this.ecno = ecno
        this.cinr = cinr
        this.tac = tac
        this.lac = lac
        this.plmn = plmn
        this.id = id
        this.cellID = cellID
        this.rxLev = rxLev
        this.color = color
    }


    override fun toString(): String {
        return "Chore(id=$id,location=$location, technology=$technology," +
                " signal strength=$signalStrength, rscp=$rscp, rsrq=$rsrq, rsrp=$rsrp," +
                " plmn=$plmn, cellID=$cellID, tac=$tac, lac=$lac, rxLev=$rxLev)"
    }

}