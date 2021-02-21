﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using Opc.Ua;
using Opc.Ua.Client;
using Opc.Ua.Configuration;

namespace OpcSimulator
{
    class Program
    {
        static int idF= 1;
        static void Main(string[] args)
        {
            var wtoken = new CancellationTokenSource();
            var task = Task.Run(async () =>
            {
                while (true)
                {
                    await Run();
                    await Task.Delay(2000, wtoken.Token); // <- await with cancellation
                }
            }, wtoken.Token);
            task.Wait();
        }

        private static Task<Session> CreateSession(
            ApplicationConfiguration config,
            EndpointDescription selectedEndpoint,
            IUserIdentity userIdentity)
        {
            var endpointConfiguration = EndpointConfiguration.Create(config);
            var endpoint = new ConfiguredEndpoint(null, selectedEndpoint, endpointConfiguration);
            return Session.Create(config, endpoint, false, "OPC UA Complex Types Client", 60000, userIdentity, null);
        }


            static string GetEnvVar(string variable, string defaultValue = null)
            {
                var v = Environment.GetEnvironmentVariable(variable)??defaultValue;
                return v??defaultValue;
            }
        async static Task Run()
        {
            var i = new Random(DateTime.Now.Millisecond).Next(19)+1;

            UserIdentity userIdentity = new UserIdentity(new AnonymousIdentityToken());
            ApplicationInstance application = new ApplicationInstance
            {
                ApplicationName = "UA Core Complex Client",
                ApplicationType = ApplicationType.Client,
                ConfigSectionName = "Opc.Ua.ComplexClient"
            };
            ApplicationConfiguration config = await application.LoadApplicationConfiguration("OpcConfig.xml", false).ConfigureAwait(false);
            var endpointURL = GetEnvVar("OPC_SERVER","opc.tcp://localhost:62541/Quickstarts/ReferenceServer");
            var selectedEndpoint = CoreClientUtils.SelectEndpoint(endpointURL, false && !true, 15000);
            var m_session = await CreateSession(config, selectedEndpoint, userIdentity).ConfigureAwait(false);



            WriteValueCollection nodesToWrite = new WriteValueCollection();

            WriteValue weight = new WriteValue();
            weight.NodeId = new NodeId("ns=2;s=Fabrication_Weight_"+i);
            weight.AttributeId = Attributes.Value;
            weight.Value = new DataValue();
            weight.Value.Value = (float)(new Random(DateTime.Now.Millisecond).Next(10000) / 100);
            nodesToWrite.Add(weight);
            WriteValue dateFabrication = new WriteValue();
            dateFabrication.NodeId = new NodeId("ns=2;s=Fabrication_Date_"+i);
            dateFabrication.AttributeId = Attributes.Value;
            dateFabrication.Value = new DataValue();
            dateFabrication.Value.Value = DateTime.Now.ToLongDateString() + " " + DateTime.Now.ToLongTimeString();
            nodesToWrite.Add(dateFabrication);
            WriteValue idFabrication = new WriteValue();
            idFabrication.NodeId = new NodeId("ns=2;s=Fabrication_Id_"+i);
            idFabrication.AttributeId = Attributes.Value;
            idFabrication.Value = new DataValue();
            idFabrication.Value.Value = idF++.ToString();
            nodesToWrite.Add(idFabrication);


            // Write the node attributes
            StatusCodeCollection results = null;
            DiagnosticInfoCollection diagnosticInfos;
            Console.WriteLine("Fabrication for machine " + i);

            // Call Write Service
            m_session.Write(null,
                            nodesToWrite,
                            out results,
                            out diagnosticInfos);

            // Validate the response
            ClientBase.ValidateResponse(results, nodesToWrite);

            foreach (StatusCode writeResult in results)
            {
                Console.WriteLine("     {0}", writeResult);
            }


        }
    }
}
