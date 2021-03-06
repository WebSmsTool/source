/*
 * Copyright 2012 software2012team23
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.tugraz.ist.akm.test.webservice.server;

import android.content.Context;
import at.tugraz.ist.akm.webservice.requestprocessor.interceptor.IRequestInterceptor;
import at.tugraz.ist.akm.webservice.server.SimpleWebServer;
import at.tugraz.ist.akm.webservice.server.WebserverProtocolConfig;

public class MockSimpleWebServer extends SimpleWebServer
{
    public MockSimpleWebServer(Context context, WebserverProtocolConfig config)
            throws Exception
    {
        super(context, config, null);
    }


    private MockSimpleWebServer(Context context) throws Exception
    {
        super(null, null, null);
    }


    @Override
    protected void setInterceptor(IRequestInterceptor reqInterceptor)
    {
    }
}
