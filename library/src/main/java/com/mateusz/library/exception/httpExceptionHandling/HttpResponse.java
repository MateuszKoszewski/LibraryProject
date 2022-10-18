package com.mateusz.library.exception.httpExceptionHandling;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class HttpResponse {

    private int httpStatusCode;

    private HttpStatus httpStatus;

    private String reason;

    private String message;


}
