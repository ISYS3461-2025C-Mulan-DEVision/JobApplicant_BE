// c:\Users\dorem\Documents\GitHub\ArchSysGroup\JobApplicant_BE\common\src\main\java\com\team\ja\common\dto\jobmanager\JobManagerPageResponse.java
package com.team.ja.common.dto.jobmanager;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Generic paginated response from Job Manager service.
 * Wraps content in a List with pagination metadata.
 * @param <T> The type of content in the page
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobManagerPageResponse<T> {
    
    private List<T> content;
    private Map<String, Object> pageable;
    
    @JsonProperty("totalPages")
    private int totalPages;
    
    @JsonProperty("totalElements")
    private int totalElements;
    
    private boolean last;
    private boolean first;
    private int size;
    private int number;
    
    @JsonProperty("numberOfElements")
    private int numberOfElements;
    
    private boolean empty;
}
