package com.enablex.multiconferencequickapp.model;

import java.io.Serializable;

public class SettingsData implements Serializable
{

        /**
         * description : Testing
         * mode : lecture
         * scheduled : false
         * adhoc : false
         * participants : 10
         * billing_code : 1234
         * auto_recording : false
         * active_talker : true
         * quality : HD
         */

        private String description;
        private String mode;
        private boolean scheduled;
        private boolean adhoc;
        private String participants;
        private int billing_code;
        private boolean auto_recording;
        private boolean active_talker;
        private String quality;

        public String getDescription()
        {
            return description;
        }

        public void setDescription(String description)
        {
            this.description = description;
        }

        public String getMode()
        {
            return mode;
        }

        public void setMode(String mode)
        {
            this.mode = mode;
        }

        public boolean isScheduled()
        {
            return scheduled;
        }

        public void setScheduled(boolean scheduled)
        {
            this.scheduled = scheduled;
        }

        public boolean isAdhoc()
        {
            return adhoc;
        }

        public void setAdhoc(boolean adhoc)
        {
            this.adhoc = adhoc;
        }

        public String getParticipants()
        {
            return participants;
        }

        public void setParticipants(String participants)
        {
            this.participants = participants;
        }

        public int getBilling_code()
        {
            return billing_code;
        }

        public void setBilling_code(int billing_code)
        {
            this.billing_code = billing_code;
        }

        public boolean isAuto_recording()
        {
            return auto_recording;
        }

        public void setAuto_recording(boolean auto_recording)
        {
            this.auto_recording = auto_recording;
        }

        public boolean isActive_talker()
        {
            return active_talker;
        }

        public void setActive_talker(boolean active_talker)
        {
            this.active_talker = active_talker;
        }

        public String getQuality()
        {
            return quality;
        }

        public void setQuality(String quality)
        {
            this.quality = quality;
        }

}
