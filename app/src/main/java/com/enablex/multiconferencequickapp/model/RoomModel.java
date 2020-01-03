package com.enablex.multiconferencequickapp.model;

public class RoomModel {
    /**
     * result : 0
     * room : {"name":"Test Dev Room","owner_ref":"fadaADADAAee","settings":{"description":"Testing","mode":"lecture","scheduled":false,"adhoc":false,"participants":"10","billing_code":1234,"auto_recording":false,"active_talker":true,"quality":"HD"},"data":{"name":"Mayank"},"created":"2018-11-28T09:26:17.837Z","room_id":"5bfe5f39e9f8e16af7dd5b77"}
     */

    private int result;
    private RoomData room;

    public int getResult()
    {
        return result;
    }

    public void setResult(int result)
    {
        this.result = result;
    }

    public RoomData getRoom()
    {
        return room;
    }

    public void setRoom(RoomData room)
    {
        this.room = room;
    }

    public static class RoomData
    {
        /**
         * name : Test Dev Room
         * owner_ref : fadaADADAAee
         * settings : {"description":"Testing","mode":"lecture","scheduled":false,"adhoc":false,"participants":"10","billing_code":1234,"auto_recording":false,"active_talker":true,"quality":"HD"}
         * data : {"name":"Mayank"}
         * created : 2018-11-28T09:26:17.837Z
         * room_id : 5bfe5f39e9f8e16af7dd5b77
         */

        private String name;
        private String owner_ref;
        private SettingsData settings;
        private UserDataModel data;
        private String created;
        private String room_id;

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public String getOwner_ref()
        {
            return owner_ref;
        }

        public void setOwner_ref(String owner_ref)
        {
            this.owner_ref = owner_ref;
        }

        public SettingsData getSettings()
        {
            return settings;
        }

        public void setSettings(SettingsData settings)
        {
            this.settings = settings;
        }

        public UserDataModel getData()
        {
            return data;
        }

        public void setData(UserDataModel data)
        {
            this.data = data;
        }

        public String getCreated()
        {
            return created;
        }

        public void setCreated(String created)
        {
            this.created = created;
        }

        public String getRoom_id()
        {
            return room_id;
        }

        public void setRoom_id(String room_id)
        {
            this.room_id = room_id;
        }
    }

    /**
     * room_id : 5b9f8f3c6306115afd6a1f07
     * role : participant
     * mode : group
     */


}
