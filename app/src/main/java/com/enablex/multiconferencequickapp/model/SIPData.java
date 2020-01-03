package com.enablex.multiconferencequickapp.model;

import java.util.List;

public class SIPData
{
        /**
         * enabled : true
         * uri : sip:292@192.168.200.116:5060
         * name : Mayank
         * auth : Yes
         * secret : No
         * aor : ["sip:292@192.168.200.116:5060","sip:292@192.168.200.116:5060"]
         */

        private boolean enabled;
        private String uri;
        private String name;
        private String auth;
        private String secret;
        private List<String> aor;

        public boolean isEnabled()
        {
            return enabled;
        }

        public void setEnabled(boolean enabled)
        {
            this.enabled = enabled;
        }

        public String getUri()
        {
            return uri;
        }

        public void setUri(String uri)
        {
            this.uri = uri;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public String getAuth()
        {
            return auth;
        }

        public void setAuth(String auth)
        {
            this.auth = auth;
        }

        public String getSecret()
        {
            return secret;
        }

        public void setSecret(String secret)
        {
            this.secret = secret;
        }

        public List<String> getAor()
        {
            return aor;
        }

        public void setAor(List<String> aor)
        {
            this.aor = aor;
        }
    }

