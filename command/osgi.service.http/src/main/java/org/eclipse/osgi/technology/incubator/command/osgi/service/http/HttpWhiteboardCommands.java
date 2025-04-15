/**
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     Stefan Bischof - initial
 */
package org.eclipse.osgi.technology.incubator.command.osgi.service.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.felix.service.command.Descriptor;
import org.apache.felix.service.command.Parameter;
import org.eclipse.osgi.technology.incubator.command.util.dtoformatter.DTOFormatter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.dto.ServiceReferenceDTO;
import org.osgi.service.http.runtime.HttpServiceRuntime;
import org.osgi.service.http.runtime.dto.DTOConstants;
import org.osgi.service.http.runtime.dto.ErrorPageDTO;
import org.osgi.service.http.runtime.dto.FailedErrorPageDTO;
import org.osgi.service.http.runtime.dto.FailedFilterDTO;
import org.osgi.service.http.runtime.dto.FailedListenerDTO;
import org.osgi.service.http.runtime.dto.FailedPreprocessorDTO;
import org.osgi.service.http.runtime.dto.FailedResourceDTO;
import org.osgi.service.http.runtime.dto.FailedServletContextDTO;
import org.osgi.service.http.runtime.dto.FailedServletDTO;
import org.osgi.service.http.runtime.dto.FilterDTO;
import org.osgi.service.http.runtime.dto.ListenerDTO;
import org.osgi.service.http.runtime.dto.PreprocessorDTO;
import org.osgi.service.http.runtime.dto.RequestInfoDTO;
import org.osgi.service.http.runtime.dto.ResourceDTO;
import org.osgi.service.http.runtime.dto.RuntimeDTO;
import org.osgi.service.http.runtime.dto.ServletContextDTO;
import org.osgi.service.http.runtime.dto.ServletDTO;
import org.osgi.util.tracker.ServiceTracker;

public class HttpWhiteboardCommands {

    final ServiceTracker<HttpServiceRuntime, HttpServiceRuntime> tracker;
    final BundleContext context;

    public HttpWhiteboardCommands(BundleContext context, DTOFormatter formatter) {
        this.context = context;
        dtos(formatter);
        tracker = new ServiceTracker<>(context, HttpServiceRuntime.class, null);
        tracker.open();
    }

    void dtos(DTOFormatter formatter) {
        formatter.build(RuntimeDTO.class).inspect().fields("*").line().fields("*").part()
                .as(dto -> String.format("[%s] port: %s", dto.serviceDTO.id, port(dto)));

        formatter.build(FailedServletContextDTO.class).inspect().fields("*").line().fields("*")
                .format("failureReason", f -> failedReason(f.failureReason)).part()
                .as(dto -> String.format("[%s] %s", dto.serviceId, dto.name));

        formatter.build(ServletContextDTO.class).inspect().fields("*").line().fields("*").part()
                .as(dto -> String.format("[%s] %s", dto.serviceId, dto.name));

        formatter.build(FailedPreprocessorDTO.class).inspect().fields("*").line().fields("*")
                .format("failureReason", f -> failedReason(f.failureReason)).part()
                .as(dto -> String.format("[%s]", dto.serviceId));

        formatter.build(PreprocessorDTO.class).inspect().fields("*").line().fields("*").part()
                .as(dto -> String.format("[%s] ", dto.serviceId));

        formatter.build(FailedServletDTO.class).inspect().fields("*").line().fields("*")
                .format("failureReason", f -> failedReason(f.failureReason)).part()
                .as(dto -> String.format("[%s] %s", dto.serviceId, dto.name));

        formatter.build(ServletDTO.class).inspect().fields("*").line().fields("*").part()
                .as(dto -> String.format("[%s] %s", dto.serviceId, dto.name));

        formatter.build(FailedResourceDTO.class).inspect().fields("*").line().fields("*")
                .format("failureReason", f -> failedReason(f.failureReason)).part()
                .as(dto -> String.format("[%s]", dto.serviceId));

        formatter.build(ResourceDTO.class).inspect().fields("*").line().fields("*").part()
                .as(dto -> String.format("[%s]", dto.serviceId));

        formatter.build(FailedFilterDTO.class).inspect().fields("*").line().fields("*")
                .format("failureReason", f -> failedReason(f.failureReason)).part()
                .as(dto -> String.format("[%s]", dto.serviceId));

        formatter.build(FilterDTO.class).inspect().fields("*").line().fields("*").part()
                .as(dto -> String.format("[%s] %s", dto.serviceId, dto.name));

        formatter.build(FailedErrorPageDTO.class).inspect().fields("*").line().fields("*")
                .format("failureReason", f -> failedReason(f.failureReason)).part()
                .as(dto -> String.format("[%s] %s", dto.serviceId, dto.name));

        formatter.build(ErrorPageDTO.class).inspect().fields("*").line().fields("*").part()
                .as(dto -> String.format("[%s] %s", dto.serviceId, dto.name));

        formatter.build(FailedListenerDTO.class).inspect().fields("*").line().fields("*")
                .format("failureReason", f -> failedReason(f.failureReason)).part()
                .as(dto -> String.format("[%s]", dto.serviceId));

        formatter.build(ListenerDTO.class).inspect().fields("*").line().fields("*").part()
                .as(dto -> String.format("[%s]", dto.serviceId));

        formatter.build(RequestInfoDTO.class).inspect().fields("*").line().fields("*").part()
                .as(dto -> String.format("[%s] ", dto.path));

        formatter.build(ServiceReferenceDTO.class).inspect().fields("*").line().fields("*").part()
                .as(dto -> String.format("[%s] port[%s] ", dto.id, port(dto)));

    }

    private List<HttpServiceRuntime> serviceRuntimes() {

        List<HttpServiceRuntime> list = new ArrayList<>();

        if (!tracker.isEmpty()) {
            for (Object httpServiceRuntimeO : tracker.getServices()) {
                var h = (HttpServiceRuntime) httpServiceRuntimeO;
                list.add(h);
            }
        }

        return list;
    }

    private HttpServiceRuntime serviceRuntime() {

        return tracker.getService();
    }

    @Descriptor("Show the RuntimeDTO of the HttpServiceRuntime")
    public List<RuntimeDTO> httpRts() throws InterruptedException {

        return serviceRuntimes().stream().map(HttpServiceRuntime::getRuntimeDTO).collect(Collectors.toList());
    }

    @Descriptor("Show the RuntimeDTO of the HttpServiceRuntime")
    public RuntimeDTO httpRt(@Descriptor("Port") @Parameter(absentValue = "", names = "-p") String port)
            throws InterruptedException {

        return runtime(port).map(HttpServiceRuntime::getRuntimeDTO).orElse(null);
    }

    @Descriptor("Show the RequestInfoDTO of the HttpServiceRuntime")
    public RequestInfoDTO httpRi(@Descriptor("Port") @Parameter(absentValue = "", names = "-p") String port,
            @Descriptor("Path") @Parameter(absentValue = "", names = "-pa") String path) throws InterruptedException {

        return runtime(port).map(runtime -> runtime.calculateRequestInfoDTO(path)).orElse(null);
    }

    private Optional<HttpServiceRuntime> runtime(String port) {

        if (port.isEmpty()) {
            return Optional.ofNullable(serviceRuntime());
        }
        return serviceRuntimes().stream().filter(runtime -> runtimeHasPort(runtime, port)).findAny();
    }

    private static boolean runtimeHasPort(HttpServiceRuntime runtime, String port) {
        return port == port(runtime);
    }

    private static String port(HttpServiceRuntime runtime) {
        var runtimeDTO = runtime.getRuntimeDTO();
        return port(runtimeDTO);
    }

    private static String port(RuntimeDTO runtimeDTO) {
        return port(runtimeDTO.serviceDTO);
    }

    private static String port(ServiceReferenceDTO srDTO) {
        var map = srDTO.properties;
        var p = map.get("org.osgi.service.http.port").toString();
        return p;
    }

    private String failedReason(int failureReason) {

        return switch (failureReason) {
        case DTOConstants.FAILURE_REASON_UNKNOWN -> "UNKNOWN";
        case DTOConstants.FAILURE_REASON_SHADOWED_BY_OTHER_SERVICE -> "FAILURE_REASON_UNKNOWN";
        case DTOConstants.FAILURE_REASON_SERVICE_NOT_GETTABLE -> "SERVICE_NOT_GETTABLE";
        case DTOConstants.FAILURE_REASON_VALIDATION_FAILED -> "VALIDATION_FAILED";
        case DTOConstants.FAILURE_REASON_SERVICE_IN_USE -> "SERVICE_IN_USE";
        case DTOConstants.FAILURE_REASON_SERVLET_WRITE_TO_LOCATION_DENIED -> "SERVLET_WRITE_TO_LOCATION_DENIED";
        case DTOConstants.FAILURE_REASON_WHITEBOARD_WRITE_TO_DEFAULT_DENIED -> "WHITEBOARD_WRITE_TO_DEFAULT_DENIED";
        case DTOConstants.FAILURE_REASON_SERVLET_READ_FROM_DEFAULT_DENIED -> "SERVLET_READ_FROM_DEFAULT_DENIED";
        case DTOConstants.FAILURE_REASON_WHITEBOARD_WRITE_TO_LOCATION_DENIED -> "WHITEBOARD_WRITE_TO_LOCATION_DENIED";
        default -> failureReason + "";
        };
    }

}
