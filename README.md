# Spring Boot Application with Prometheus and Grafana Monitoring

This repository hosts a Spring Boot application configured for monitoring and observability using Prometheus and Grafana. The application exposes metrics and health endpoints, which Prometheus can scrape, and Grafana can visualize.

## Table of Contents
- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Setup and Configuration](#setup-and-configuration)
- [Prometheus Configuration](#prometheus-configuration)
- [Exposing the Service with Istio](#exposing-the-service-with-istio)
- [Setting Up Grafana](#setting-up-grafana)
- [Accessing Metrics and Health Endpoints](#accessing-metrics-and-health-endpoints)
- [Running the Application](#running-the-application)
- [Monitoring with Prometheus and Grafana](#monitoring-with-prometheus-and-grafana)
- [Troubleshooting](#troubleshooting)

## Overview

This Spring Boot application is set up to expose essential monitoring data via Prometheus-compatible endpoints. Metrics are available at `/metrics`, and health information is accessible at `/health`. Prometheus scrapes this data and Grafana visualizes it, allowing you to monitor your application’s health and performance.

## Prerequisites

Ensure the following tools are installed on your system:
- Java 11+
- Maven
- Docker (optional, for containerized deployment)
- Prometheus (to scrape and visualize metrics)
- Grafana (to create dashboards for metric visualization)
- Istio (for service mesh and traffic management)

## Setup and Configuration

The following configuration settings are used to expose the necessary endpoints for Prometheus monitoring:

```properties
spring.application.name=demo
SERVICE_B_URL=http://default-url-for-service-b

logging.level.root=INFO
logging.level.org.springframework=INFO
logging.level.com.example=INFO

management.endpoints.web.base-path=/
management.endpoints.web.exposure.include=metrics,health
management.metrics.export.prometheus.enabled=true
management.endpoints.web.path-mapping.metrics=metrics
```

- **`management.endpoints.web.exposure.include=metrics,health`**: Exposes both metrics and health endpoints.
- **`management.endpoints.web.base-path=/`**: Sets the base path for management endpoints to root (`/`).
- **`management.endpoints.web.path-mapping.metrics=metrics`**: Maps the metrics endpoint to `/metrics`.

## Prometheus Configuration

To enable Prometheus to scrape data from this application, add the following scrape configuration to your Prometheus `prometheus.yml` file:

```yaml
scrape_configs:
  - job_name: 'spring-boot-app'
    static_configs:
      - targets: ['<your-service-url>:8080']
```

Replace `<your-service-url>:8080` with the address of your running service.

## Exposing the Service with Istio

In an Istio-enabled environment, you can expose the application for external access using a `VirtualService` and a `Gateway`. Here’s an example configuration:

1. **Create a Gateway** to allow traffic to reach your service:

   ```yaml
   apiVersion: networking.istio.io/v1alpha3
   kind: Gateway
   metadata:
     name: monitoring-gateway
     namespace: demo
   spec:
     selector:
       istio: ingressgateway # Use Istio's ingress gateway
     servers:
       - port:
           number: 80
           name: http
           protocol: HTTP
         hosts:
           - '*'
   ```

2. **Create a VirtualService** to route traffic to the application:

   ```yaml
   apiVersion: networking.istio.io/v1alpha3
   kind: VirtualService
   metadata:
     name: service-a
     namespace: demo
   spec:
     hosts:
       - "service-a.sumangcodes.info" # Your external host domain
     gateways:
       - monitoring-gateway
     http:
       - match:
           - uri:
               prefix: "/metrics"
           - uri:
               prefix: "/health"
         route:
           - destination:
               host: service-a.demo.svc.cluster.local
               port:
                 number: 8080
   ```

   - **`monitoring-gateway`**: Specifies the gateway through which the traffic will be routed.
   - **`service-a.sumangcodes.info`**: Replace with your domain or host to access the application from outside the cluster.
   - **`service-a.demo.svc.cluster.local`**: Internal service address in Kubernetes.

3. Apply the configurations to your cluster:

   ```bash
   kubectl apply -f monitoring-gateway.yaml
   kubectl apply -f service-a-virtualservice.yaml
   ```

This configuration will expose your `/metrics` and `/health` endpoints for monitoring.

## Setting Up Grafana

Grafana can be used to visualize the metrics collected by Prometheus. Follow these steps to set up Grafana and connect it to Prometheus:

1. **Deploy Grafana** (if not already installed):

   ```bash
   kubectl apply -f https://raw.githubusercontent.com/istio/istio/release-1.14/samples/addons/grafana.yaml
   ```

2. **Access Grafana**:

   ```bash
   istioctl dashboard grafana
   ```

   This command will open the Grafana UI in your browser.

3. **Add Prometheus as a Data Source**:
   - Go to **Configuration > Data Sources** in Grafana.
   - Click **Add data source** and select **Prometheus**.
   - Set the URL to your Prometheus instance (e.g., `http://prometheus-server:9090` if running in-cluster).
   - Click **Save & Test** to confirm the connection.

4. **Import or Create Dashboards**:
   - Grafana provides pre-built dashboards for Spring Boot applications and JVM metrics. You can import a dashboard by going to **Dashboards > Manage > Import** and entering the dashboard ID (e.g., [4701 for JVM (Micrometer)](https://grafana.com/grafana/dashboards/4701)).

## Accessing Metrics and Health Endpoints

- **Metrics**: `http://service-a.sumangcodes.info/metrics`
- **Health**: `http://service-a.sumangcodes.info/health`

## Running the Application

To start the application, run the following command:

```bash
./mvnw spring-boot:run
```

Alternatively, you can package and run it as a JAR:

```bash
./mvnw clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

## Monitoring with Prometheus and Grafana

1. Start Prometheus and Grafana in your environment.
2. Ensure Prometheus is scraping the `/metrics` endpoint of your Spring Boot application.
3. In Grafana, create dashboards or import pre-built ones to visualize metrics such as JVM usage, HTTP requests, and application uptime.

## Troubleshooting

- **Endpoint Unreachable**: Verify that the application is running on port `8080` and that `/metrics` and `/health` endpoints are exposed as per configuration.
- **Prometheus Scraping Error**: Ensure the correct URL is specified in the Prometheus `prometheus.yml` configuration file.
- **Grafana Connection Issue**: Confirm that the Prometheus data source is correctly configured in Grafana.

