package dev.pollito.poof.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.pollito.poof.model.Contracts;
import dev.pollito.poof.model.GenerateRequest;
import dev.pollito.poof.model.Options;
import dev.pollito.poof.model.ProjectMetadata;
import dev.pollito.poof.service.impl.GeneratePoofServiceImpl;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GeneratePoofServiceTest {
  @InjectMocks private GeneratePoofServiceImpl generatePoofService;

  private static final String PROJECT_METADATA_GROUP = "dev.pollito";
  private static final String PROJECT_METADATA_ARTIFACT = "poof";
  private static final String PROJECT_METADATA_DESCRIPTION =
      "poof - Pollito Over Opinionated Framework";
  private static final String BASE_64_PETSORE_YAML =
      "b3BlbmFwaTogIjMuMC4wIgppbmZvOgogIHZlcnNpb246IDEuMC4wCiAgdGl0bGU6IFN3YWdnZXIgUGV0c3RvcmUKICBsaWNlbnNlOgogICAgbmFtZTogTUlUCnNlcnZlcnM6CiAgLSB1cmw6IGh0dHA6Ly9wZXRzdG9yZS5zd2FnZ2VyLmlvL3YxCnBhdGhzOgogIC9wZXRzOgogICAgZ2V0OgogICAgICBzdW1tYXJ5OiBMaXN0IGFsbCBwZXRzCiAgICAgIG9wZXJhdGlvbklkOiBsaXN0UGV0cwogICAgICB0YWdzOgogICAgICAgIC0gcGV0cwogICAgICBwYXJhbWV0ZXJzOgogICAgICAgIC0gbmFtZTogbGltaXQKICAgICAgICAgIGluOiBxdWVyeQogICAgICAgICAgZGVzY3JpcHRpb246IEhvdyBtYW55IGl0ZW1zIHRvIHJldHVybiBhdCBvbmUgdGltZSAobWF4IDEwMCkKICAgICAgICAgIHJlcXVpcmVkOiBmYWxzZQogICAgICAgICAgc2NoZW1hOgogICAgICAgICAgICB0eXBlOiBpbnRlZ2VyCiAgICAgICAgICAgIG1heGltdW06IDEwMAogICAgICAgICAgICBmb3JtYXQ6IGludDMyCiAgICAgIHJlc3BvbnNlczoKICAgICAgICAnMjAwJzoKICAgICAgICAgIGRlc2NyaXB0aW9uOiBBIHBhZ2VkIGFycmF5IG9mIHBldHMKICAgICAgICAgIGhlYWRlcnM6CiAgICAgICAgICAgIHgtbmV4dDoKICAgICAgICAgICAgICBkZXNjcmlwdGlvbjogQSBsaW5rIHRvIHRoZSBuZXh0IHBhZ2Ugb2YgcmVzcG9uc2VzCiAgICAgICAgICAgICAgc2NoZW1hOgogICAgICAgICAgICAgICAgdHlwZTogc3RyaW5nCiAgICAgICAgICBjb250ZW50OgogICAgICAgICAgICBhcHBsaWNhdGlvbi9qc29uOiAgICAKICAgICAgICAgICAgICBzY2hlbWE6CiAgICAgICAgICAgICAgICAkcmVmOiAiIy9jb21wb25lbnRzL3NjaGVtYXMvUGV0cyIKICAgICAgICBkZWZhdWx0OgogICAgICAgICAgZGVzY3JpcHRpb246IHVuZXhwZWN0ZWQgZXJyb3IKICAgICAgICAgIGNvbnRlbnQ6CiAgICAgICAgICAgIGFwcGxpY2F0aW9uL2pzb246CiAgICAgICAgICAgICAgc2NoZW1hOgogICAgICAgICAgICAgICAgJHJlZjogIiMvY29tcG9uZW50cy9zY2hlbWFzL0Vycm9yIgogICAgcG9zdDoKICAgICAgc3VtbWFyeTogQ3JlYXRlIGEgcGV0CiAgICAgIG9wZXJhdGlvbklkOiBjcmVhdGVQZXRzCiAgICAgIHRhZ3M6CiAgICAgICAgLSBwZXRzCiAgICAgIHJlcXVlc3RCb2R5OgogICAgICAgIGNvbnRlbnQ6CiAgICAgICAgICBhcHBsaWNhdGlvbi9qc29uOgogICAgICAgICAgICBzY2hlbWE6CiAgICAgICAgICAgICAgJHJlZjogJyMvY29tcG9uZW50cy9zY2hlbWFzL1BldCcKICAgICAgICByZXF1aXJlZDogdHJ1ZQogICAgICByZXNwb25zZXM6CiAgICAgICAgJzIwMSc6CiAgICAgICAgICBkZXNjcmlwdGlvbjogTnVsbCByZXNwb25zZQogICAgICAgIGRlZmF1bHQ6CiAgICAgICAgICBkZXNjcmlwdGlvbjogdW5leHBlY3RlZCBlcnJvcgogICAgICAgICAgY29udGVudDoKICAgICAgICAgICAgYXBwbGljYXRpb24vanNvbjoKICAgICAgICAgICAgICBzY2hlbWE6CiAgICAgICAgICAgICAgICAkcmVmOiAiIy9jb21wb25lbnRzL3NjaGVtYXMvRXJyb3IiCiAgL3BldHMve3BldElkfToKICAgIGdldDoKICAgICAgc3VtbWFyeTogSW5mbyBmb3IgYSBzcGVjaWZpYyBwZXQKICAgICAgb3BlcmF0aW9uSWQ6IHNob3dQZXRCeUlkCiAgICAgIHRhZ3M6CiAgICAgICAgLSBwZXRzCiAgICAgIHBhcmFtZXRlcnM6CiAgICAgICAgLSBuYW1lOiBwZXRJZAogICAgICAgICAgaW46IHBhdGgKICAgICAgICAgIHJlcXVpcmVkOiB0cnVlCiAgICAgICAgICBkZXNjcmlwdGlvbjogVGhlIGlkIG9mIHRoZSBwZXQgdG8gcmV0cmlldmUKICAgICAgICAgIHNjaGVtYToKICAgICAgICAgICAgdHlwZTogc3RyaW5nCiAgICAgIHJlc3BvbnNlczoKICAgICAgICAnMjAwJzoKICAgICAgICAgIGRlc2NyaXB0aW9uOiBFeHBlY3RlZCByZXNwb25zZSB0byBhIHZhbGlkIHJlcXVlc3QKICAgICAgICAgIGNvbnRlbnQ6CiAgICAgICAgICAgIGFwcGxpY2F0aW9uL2pzb246CiAgICAgICAgICAgICAgc2NoZW1hOgogICAgICAgICAgICAgICAgJHJlZjogIiMvY29tcG9uZW50cy9zY2hlbWFzL1BldCIKICAgICAgICBkZWZhdWx0OgogICAgICAgICAgZGVzY3JpcHRpb246IHVuZXhwZWN0ZWQgZXJyb3IKICAgICAgICAgIGNvbnRlbnQ6CiAgICAgICAgICAgIGFwcGxpY2F0aW9uL2pzb246CiAgICAgICAgICAgICAgc2NoZW1hOgogICAgICAgICAgICAgICAgJHJlZjogIiMvY29tcG9uZW50cy9zY2hlbWFzL0Vycm9yIgpjb21wb25lbnRzOgogIHNjaGVtYXM6CiAgICBQZXQ6CiAgICAgIHR5cGU6IG9iamVjdAogICAgICByZXF1aXJlZDoKICAgICAgICAtIGlkCiAgICAgICAgLSBuYW1lCiAgICAgIHByb3BlcnRpZXM6CiAgICAgICAgaWQ6CiAgICAgICAgICB0eXBlOiBpbnRlZ2VyCiAgICAgICAgICBmb3JtYXQ6IGludDY0CiAgICAgICAgbmFtZToKICAgICAgICAgIHR5cGU6IHN0cmluZwogICAgICAgIHRhZzoKICAgICAgICAgIHR5cGU6IHN0cmluZwogICAgUGV0czoKICAgICAgdHlwZTogYXJyYXkKICAgICAgbWF4SXRlbXM6IDEwMAogICAgICBpdGVtczoKICAgICAgICAkcmVmOiAiIy9jb21wb25lbnRzL3NjaGVtYXMvUGV0IgogICAgRXJyb3I6CiAgICAgIHR5cGU6IG9iamVjdAogICAgICByZXF1aXJlZDoKICAgICAgICAtIGNvZGUKICAgICAgICAtIG1lc3NhZ2UKICAgICAgcHJvcGVydGllczoKICAgICAgICBjb2RlOgogICAgICAgICAgdHlwZTogaW50ZWdlcgogICAgICAgICAgZm9ybWF0OiBpbnQzMgogICAgICAgIG1lc3NhZ2U6CiAgICAgICAgICB0eXBlOiBzdHJpbmcK";
  private static final String BASE_64_MEDIASTACK_YAML =
      "b3BlbmFwaTogMy4wLjANCmluZm86DQogIHRpdGxlOiBNZWRpYXN0YWNrIEFQSQ0KICB2ZXJzaW9uOiAxLjAuMA0KICBkZXNjcmlwdGlvbjogQVBJIGZvciBkZWxpdmVyaW5nIHdvcmxkd2lkZSBuZXdzLCBoZWFkbGluZXMgYW5kIGJsb2cgYXJ0aWNsZXMgaW4gcmVhbC10aW1lDQpzZXJ2ZXJzOg0KICAtIHVybDogaHR0cDovL2FwaS5tZWRpYXN0YWNrLmNvbQ0KDQpwYXRoczoNCiAgL3YxL25ld3M6DQogICAgZ2V0Og0KICAgICAgdGFnczoNCiAgICAgICAgLSBOZXdzDQogICAgICBzdW1tYXJ5OiBGdWxsIHNldCBvZiBhdmFpbGFibGUgcmVhbC10aW1lIG5ld3MgYXJ0aWNsZXMNCiAgICAgIHBhcmFtZXRlcnM6DQogICAgICAgIC0gbmFtZTogYWNjZXNzX2tleQ0KICAgICAgICAgIGluOiBxdWVyeQ0KICAgICAgICAgIHJlcXVpcmVkOiB0cnVlDQogICAgICAgICAgc2NoZW1hOg0KICAgICAgICAgICAgdHlwZTogc3RyaW5nDQogICAgICAgICAgZGVzY3JpcHRpb246IFVzZSB0aGlzIHBhcmFtZXRlciB0byBzcGVjaWZ5IHlvdXIgdW5pcXVlIEFQSSBhY2Nlc3Mga2V5LCB3aGljaCBpcyBzaG93biB3aGVuIHlvdSBsb2cgaW4gdG8geW91ciBbYWNjb3VudCBkYXNoYm9hcmRdKGh0dHBzOi8vbWVkaWFzdGFjay5jb20vZGFzaGJvYXJkKS4NCiAgICAgICAgLSBuYW1lOiBzb3VyY2VzDQogICAgICAgICAgaW46IHF1ZXJ5DQogICAgICAgICAgc2NoZW1hOg0KICAgICAgICAgICAgdHlwZTogc3RyaW5nDQogICAgICAgICAgZGVzY3JpcHRpb246IFVzZSB0aGlzIHBhcmFtZXRlciB0byBpbmNsdWRlIG9yIGV4Y2x1ZGUgb25lIG9yIG11bHRpcGxlIGNvbW1hLXNlcGFyYXRlZCBuZXdzIHNvdXJjZXMuDQogICAgICAgICAgZXhhbXBsZTogY25uLC1iYmMNCiAgICAgICAgLSBuYW1lOiBjYXRlZ29yaWVzDQogICAgICAgICAgaW46IHF1ZXJ5DQogICAgICAgICAgc2NoZW1hOg0KICAgICAgICAgICAgdHlwZTogc3RyaW5nDQogICAgICAgICAgZGVzY3JpcHRpb246IFVzZSB0aGlzIHBhcmFtZXRlciB0byBpbmNsdWRlIG9yIGV4Y2x1ZGUgb25lIG9yIG11bHRpcGxlIGNvbW1hLXNlcGFyYXRlZCBuZXdzIGNhdGVnb3JpZXMuIEF2YWlsYWJsZSBjYXRlZ29yaWVzIFtnZW5lcmFsLCBidXNpbmVzcywgZW50ZXJ0YWlubWVudCwgaGVhbHRoLCBzY2llbmNlLCBzcG9ydHMsIHRlY2hub2xvZ3ldLg0KICAgICAgICAgIGV4YW1wbGU6IGJ1c2luZXNzLC1zcG9ydHMNCiAgICAgICAgLSBuYW1lOiBjb3VudHJpZXMNCiAgICAgICAgICBpbjogcXVlcnkNCiAgICAgICAgICBzY2hlbWE6DQogICAgICAgICAgICB0eXBlOiBzdHJpbmcNCiAgICAgICAgICBkZXNjcmlwdGlvbjogVXNlIHRoaXMgcGFyYW1ldGVyIHRvIGluY2x1ZGUgb3IgZXhjbHVkZSBvbmUgb3IgbXVsdGlwbGUgY29tbWEtc2VwYXJhdGVkIGNvdW50cmllcy4gW0F2YWlsYWJsZSBjb3VudHJpZXNdKGh0dHBzOi8vbWVkaWFzdGFjay5jb20vc291cmNlcykuDQogICAgICAgICAgZXhhbXBsZTogYXUsLXVzDQogICAgICAgIC0gbmFtZTogbGFuZ3VhZ2VzDQogICAgICAgICAgaW46IHF1ZXJ5DQogICAgICAgICAgc2NoZW1hOg0KICAgICAgICAgICAgdHlwZTogc3RyaW5nDQogICAgICAgICAgZGVzY3JpcHRpb246IFVzZSB0aGlzIHBhcmFtZXRlciB0byBpbmNsdWRlIG9yIGV4Y2x1ZGUgb25lIG9yIG11bHRpcGxlIGNvbW1hLXNlcGFyYXRlZCBsYW5ndWFnZXMuIEF2YWlsYWJsZSBsYW5ndWFnZXMgW2FyLCBkZSwgZW4sIGVzLCBmciwgaGUsIGl0LCBubCwgbm8sIHB0LCBydSwgc2UsIHpoXS4NCiAgICAgICAgICBleGFtcGxlOiBlbiwtZGUNCiAgICAgICAgLSBuYW1lOiBrZXl3b3Jkcw0KICAgICAgICAgIGluOiBxdWVyeQ0KICAgICAgICAgIHNjaGVtYToNCiAgICAgICAgICAgIHR5cGU6IHN0cmluZw0KICAgICAgICAgIGRlc2NyaXB0aW9uOiBVc2UgdGhpcyBwYXJhbWV0ZXIgdG8gc2VhY2ggZm9yIHNlbnRlbmNlcywgeW91IGNhbiBhbHNvIGV4Y2x1ZGUgd29yZHMgdGhhdCB5b3UgZG8gbm90IHdhbnQgdG8gYXBwZWFyIGluIHlvdXIgc2VhcmNoIHJlc3VsdHMuDQogICAgICAgICAgZXhhbXBsZTogbmV3IG1vdmllcyAyMDIxIC1tYXRyaXgNCiAgICAgICAgLSBuYW1lOiBkYXRlDQogICAgICAgICAgaW46IHF1ZXJ5DQogICAgICAgICAgc2NoZW1hOg0KICAgICAgICAgICAgdHlwZTogc3RyaW5nDQogICAgICAgICAgZGVzY3JpcHRpb246IFVzZSB0aGlzIHBhcmFtZXRlciB0byBzcGVjaWZ5IGEgZGF0ZSBvciBkYXRlIHJhbmdlLg0KICAgICAgICAgIGV4YW1wbGU6IDIwMjAtMTItMjQsMjAyMC0xMi0zMQ0KICAgICAgICAtIG5hbWU6IHNvcnQNCiAgICAgICAgICBpbjogcXVlcnkNCiAgICAgICAgICBzY2hlbWE6DQogICAgICAgICAgICAkcmVmOiAnIy9jb21wb25lbnRzL3NjaGVtYXMvU29ydEVudW0nDQogICAgICAgICAgZGVzY3JpcHRpb246IFVzZSB0aGlzIHBhcmFtZXRlciB0byBzcGVjaWZ5IGEgc29ydGluZyBvcmRlci4NCiAgICAgICAgLSBuYW1lOiBsaW1pdA0KICAgICAgICAgIGluOiBxdWVyeQ0KICAgICAgICAgIHNjaGVtYToNCiAgICAgICAgICAgIHR5cGU6IGludGVnZXINCiAgICAgICAgICAgIGRlZmF1bHQ6IDI1DQogICAgICAgICAgICBtYXhpbXVtOiAxMDANCiAgICAgICAgICBkZXNjcmlwdGlvbjogVXNlIHRoaXMgcGFyYW1ldGVyIHRvIHNwZWNpZnkgYSBwYWdpbmF0aW9uIGxpbWl0IChudW1iZXIgb2YgcmVzdWx0cyBwZXIgcGFnZSkgZm9yIHlvdXIgQVBJIHJlcXVlc3QuDQogICAgICAgIC0gbmFtZTogb2Zmc2V0DQogICAgICAgICAgaW46IHF1ZXJ5DQogICAgICAgICAgc2NoZW1hOg0KICAgICAgICAgICAgdHlwZTogaW50ZWdlcg0KICAgICAgICAgICAgZGVmYXVsdDogMA0KICAgICAgICAgIGRlc2NyaXB0aW9uOiBVc2UgdGhpcyBwYXJhbWV0ZXIgdG8gc3BlY2lmeSBhIHBhZ2luYXRpb24gb2Zmc2V0IHZhbHVlIGZvciB5b3VyIEFQSSByZXF1ZXN0Lg0KICAgICAgcmVzcG9uc2VzOg0KICAgICAgICAnMjAwJzoNCiAgICAgICAgICBkZXNjcmlwdGlvbjogU3VjY2Vzc2Z1bCByZXNwb25zZQ0KICAgICAgICAgIGNvbnRlbnQ6DQogICAgICAgICAgICBhcHBsaWNhdGlvbi9qc29uOg0KICAgICAgICAgICAgICBzY2hlbWE6DQogICAgICAgICAgICAgICAgJHJlZjogJyMvY29tcG9uZW50cy9zY2hlbWFzL0FydGljbGVzJw0KDQogICAgICAgICc1MDAnOg0KICAgICAgICAgIGRlc2NyaXB0aW9uOiBJbnRlcm5hbCBTZXJ2ZXIgRXJyb3INCiAgICAgICAgICBjb250ZW50Og0KICAgICAgICAgICAgYXBwbGljYXRpb24vanNvbjoNCiAgICAgICAgICAgICAgc2NoZW1hOg0KICAgICAgICAgICAgICAgICRyZWY6ICcjL2NvbXBvbmVudHMvc2NoZW1hcy9NZWRpYVN0YWNrRXJyb3InDQoNCmNvbXBvbmVudHM6DQogIHNjaGVtYXM6DQogICAgQXJ0aWNsZXM6DQogICAgICB0eXBlOiBvYmplY3QNCiAgICAgIHByb3BlcnRpZXM6DQogICAgICAgIHBhZ2luYXRpb246DQogICAgICAgICAgJHJlZjogJyMvY29tcG9uZW50cy9zY2hlbWFzL0FydGljbGVzUGFnaW5hdGlvbicNCiAgICAgICAgZGF0YToNCiAgICAgICAgICB0eXBlOiBhcnJheQ0KICAgICAgICAgIGl0ZW1zOg0KICAgICAgICAgICAgJHJlZjogJyMvY29tcG9uZW50cy9zY2hlbWFzL0FydGljbGUnDQogICAgQXJ0aWNsZXNQYWdpbmF0aW9uOg0KICAgICAgdHlwZTogb2JqZWN0DQogICAgICBwcm9wZXJ0aWVzOg0KICAgICAgICBsaW1pdDoNCiAgICAgICAgICB0eXBlOiBpbnRlZ2VyDQogICAgICAgICAgZGVzY3JpcHRpb246IFJldHVybnMgeW91ciBwYWdpbmF0aW9uIGxpbWl0IHZhbHVlLg0KICAgICAgICBvZmZzZXQ6DQogICAgICAgICAgdHlwZTogaW50ZWdlcg0KICAgICAgICAgIGRlc2NyaXB0aW9uOiBSZXR1cm5zIHlvdXIgcGFnaW5hdGlvbiBvZmZzZXQgdmFsdWUuDQogICAgICAgIGNvdW50Og0KICAgICAgICAgIHR5cGU6IGludGVnZXINCiAgICAgICAgICBkZXNjcmlwdGlvbjogUmV0dXJucyB0aGUgcmVzdWx0cyBjb3VudCBvbiB0aGUgY3VycmVudCBwYWdlLg0KICAgICAgICB0b3RhbDoNCiAgICAgICAgICB0eXBlOiBpbnRlZ2VyDQogICAgICAgICAgZGVzY3JpcHRpb246IFJldHVybnMgdGhlIHRvdGFsIGNvdW50IG9mIHJlc3VsdHMgYXZhaWxhYmxlLg0KICAgIEFydGljbGU6DQogICAgICB0eXBlOiBvYmplY3QNCiAgICAgIHByb3BlcnRpZXM6DQogICAgICAgIGF1dGhvcjoNCiAgICAgICAgICB0eXBlOiBzdHJpbmcNCiAgICAgICAgICBkZXNjcmlwdGlvbjogUmV0dXJucyB0aGUgbmFtZSBvZiB0aGUgYXV0aG9yIG9mIHRoZSBnaXZlbiBuZXdzIGFydGljbGUuDQogICAgICAgIHRpdGxlOg0KICAgICAgICAgIHR5cGU6IHN0cmluZw0KICAgICAgICAgIGRlc2NyaXB0aW9uOiBSZXR1cm5zIHRoZSB0aXRsZSB0ZXh0IG9mIHRoZSBnaXZlbiBuZXdzIGFydGljbGUuDQogICAgICAgIGRlc2NyaXB0aW9uOg0KICAgICAgICAgIHR5cGU6IHN0cmluZw0KICAgICAgICAgIGRlc2NyaXB0aW9uOiBSZXR1cm5zIHRoZSBkZXNjcmlwdGlvbiB0ZXh0IG9mIHRoZSBnaXZlbiBuZXdzIGFydGljbGUuDQogICAgICAgIHVybDoNCiAgICAgICAgICB0eXBlOiBzdHJpbmcNCiAgICAgICAgICBkZXNjcmlwdGlvbjogUmV0dXJucyB0aGUgVVJMIGxlYWRpbmcgdG8gdGhlIGdpdmVuIG5ld3MgYXJ0aWNsZS4NCiAgICAgICAgaW1hZ2U6DQogICAgICAgICAgdHlwZTogc3RyaW5nDQogICAgICAgICAgZGVzY3JpcHRpb246IFJldHVybnMgYW4gaW1hZ2UgVVJMIGFzc29jaWF0ZWQgd2l0aCB0aGUgZ2l2ZW4gbmV3cyBhcnRpY2xlLg0KICAgICAgICBjYXRlZ29yeToNCiAgICAgICAgICBhbGxPZjoNCiAgICAgICAgICAgIC0gJHJlZjogJyMvY29tcG9uZW50cy9zY2hlbWFzL0NhdGVnb3J5RW51bScNCiAgICAgICAgICBkZXNjcmlwdGlvbjogUmV0dXJucyB0aGUgY2F0ZWdvcnkgYXNzb2NpYXRlZCB3aXRoIHRoZSBnaXZlbiBuZXdzIGFydGljbGUuDQogICAgICAgIGxhbmd1YWdlOg0KICAgICAgICAgIGFsbE9mOg0KICAgICAgICAgICAgLSAkcmVmOiAnIy9jb21wb25lbnRzL3NjaGVtYXMvTGFuZ3VhZ2VFbnVtJw0KICAgICAgICAgIGRlc2NyaXB0aW9uOiBSZXR1cm5zIHRoZSBsYW5ndWFnZSB0aGUgZ2l2ZW4gbmV3cyBhcnRpY2xlIGlzIGluLg0KICAgICAgICBjb3VudHJ5Og0KICAgICAgICAgIGFsbE9mOg0KICAgICAgICAgICAgLSAkcmVmOiAnIy9jb21wb25lbnRzL3NjaGVtYXMvQ291bnRyeUVudW0nDQogICAgICAgICAgZGVzY3JpcHRpb246IFJldHVybnMgdGhlIGNvdW50cnkgY29kZSBhc3NvY2lhdGVkIHdpdGggdGhlIGdpdmVuIG5ld3MgYXJ0aWNsZS4NCiAgICAgICAgcHVibGlzaGVkX2F0Og0KICAgICAgICAgIHR5cGU6IHN0cmluZw0KICAgICAgICAgIGZvcm1hdDogZGF0ZS10aW1lDQogICAgICAgICAgZGVzY3JpcHRpb246IFRoZSBkYXRlIGFuZCB0aW1lIHdoZW4gdGhlIHB1YmxpY2F0aW9uIG9jY3VycmVkIGluIElTTyA4NjAxIGZvcm1hdC4NCiAgICAgICAgICBleGFtcGxlOiAiMjAyNC0wMS0wNFQxNTozMDowMFoiDQogICAgQ291bnRyeUVudW06DQogICAgICB0eXBlOiBzdHJpbmcNCiAgICAgIGVudW06IFsgImFyIiwgImF1IiwgImF0IiwgImJlIiwgImJyIiwgImJnIiwgImNhIiwgImNuIiwgImNvIiwgImN6IiwgImVnIiwgImZyIiwgImRlIiwgImdyIiwgImhrIiwgImh1IiwgImluIiwgImlkIiwgImllIiwgImlsIiwgIml0IiwgImpwIiwgImx2IiwgImx0IiwgIm15IiwgIm14IiwgIm1hIiwgIm5sIiwgIm56IiwgIm5nIiwgIm5vIiwgInBoIiwgInBsIiwgInB0IiwgInJvIiwgInNhIiwgInJzIiwgInNnIiwgInNrIiwgInNpIiwgInphIiwgImtyIiwgInNlIiwgImNoIiwgInR3IiwgInRoIiwgInRyIiwgImFlIiwgInVhIiwgImdiIiwgInVzIiwgInZlIiBdDQogICAgTGFuZ3VhZ2VFbnVtOg0KICAgICAgdHlwZTogc3RyaW5nDQogICAgICBlbnVtOiBbICJhciIsICJkZSIsICJlbiIsICJlcyIsICJmciIsICJoZSIsICJpdCIsICJubCIsICJubyIsICJwdCIsICJydSIsICJzZSIsICJ6aCIgXQ0KICAgIENhdGVnb3J5RW51bToNCiAgICAgIHR5cGU6IHN0cmluZw0KICAgICAgZW51bTogWyAiZ2VuZXJhbCIsICJidXNpbmVzcyIsICJlbnRlcnRhaW5tZW50IiwgImhlYWx0aCIsICJzY2llbmNlIiwgInNwb3J0cyIsICJ0ZWNobm9sb2d5IiBdDQogICAgU29ydEVudW06DQogICAgICB0eXBlOiBzdHJpbmcNCiAgICAgIGVudW06IFsgInB1Ymxpc2hlZF9kZXNjIiwgInB1Ymxpc2hlZF9hc2MiLCAicG9wdWxhcml0eSIgXQ0KICAgICAgZGVmYXVsdDogInB1Ymxpc2hlZF9kZXNjIg0KICAgIE1lZGlhU3RhY2tFcnJvcjoNCiAgICAgIHR5cGU6IG9iamVjdA0KICAgICAgcHJvcGVydGllczoNCiAgICAgICAgZXJyb3I6DQogICAgICAgICAgdHlwZTogb2JqZWN0DQogICAgICAgICAgZGVzY3JpcHRpb246IGRldGFpbHMgYWJvdXQgdGhlIGVycm9yIHRoYXQgb2NjdXJyZWQuDQogICAgICAgICAgcHJvcGVydGllczoNCiAgICAgICAgICAgIGNvZGU6DQogICAgICAgICAgICAgIHR5cGU6IHN0cmluZw0KICAgICAgICAgICAgbWVzc2FnZToNCiAgICAgICAgICAgICAgdHlwZTogc3RyaW5nDQogICAgICAgICAgICBjb250ZXh0Og0KICAgICAgICAgICAgICB0eXBlOiBvYmplY3QNCiAgICAgICAgICAgICAgcHJvcGVydGllczoNCiAgICAgICAgICAgICAgICBkYXRlOg0KICAgICAgICAgICAgICAgICAgdHlwZTogYXJyYXkNCiAgICAgICAgICAgICAgICAgIGl0ZW1zOg0KICAgICAgICAgICAgICAgICAgICB0eXBlOiBzdHJpbmcNCg==";
  private static final String BASE_64_WEATHERSTACK_YAML =
      "b3BlbmFwaTogMy4wLjANCmluZm86DQogIHRpdGxlOiBXZWF0aGVyc3RhY2sgQVBJDQogIHZlcnNpb246IDEuMC4wDQogIGRlc2NyaXB0aW9uOiBBUEkgZm9yIGN1cnJlbnQgd2VhdGhlciBkYXRhDQpzZXJ2ZXJzOg0KICAtIHVybDogaHR0cDovL2FwaS53ZWF0aGVyc3RhY2suY29tDQoNCnBhdGhzOg0KICAvY3VycmVudDoNCiAgICBnZXQ6DQogICAgICB0YWdzOg0KICAgICAgICAtIFdlYXRoZXINCiAgICAgIHN1bW1hcnk6IEdldCBjdXJyZW50IHdlYXRoZXIgZGF0YQ0KICAgICAgcGFyYW1ldGVyczoNCiAgICAgICAgLSBuYW1lOiBhY2Nlc3Nfa2V5DQogICAgICAgICAgaW46IHF1ZXJ5DQogICAgICAgICAgcmVxdWlyZWQ6IHRydWUNCiAgICAgICAgICBzY2hlbWE6DQogICAgICAgICAgICB0eXBlOiBzdHJpbmcNCiAgICAgICAgICBkZXNjcmlwdGlvbjogWW91ciBBUEkgYWNjZXNzIGtleSwgd2hpY2ggY2FuIGJlIGZvdW5kIGluIHlvdXIgW2FjY2NvdW50IGRhc2hib2FyZF0oaHR0cHM6Ly93ZWF0aGVyc3RhY2suY29tL2Rhc2hib2FyZCkNCiAgICAgICAgLSBuYW1lOiBxdWVyeQ0KICAgICAgICAgIGluOiBxdWVyeQ0KICAgICAgICAgIHJlcXVpcmVkOiB0cnVlDQogICAgICAgICAgc2NoZW1hOg0KICAgICAgICAgICAgdHlwZTogc3RyaW5nDQogICAgICAgICAgZGVzY3JpcHRpb246IFVzZSB0aGlzIHBhcmFtZXRlciB0byBwYXNzIGEgc2luZ2xlIGxvY2F0aW9uIG9yIG11bHRpcGxlIHNlbWljb2xvbi1zZXBhcmF0ZWQgbG9jYXRpb24gaWRlbnRpZmllcnMgdG8gdGhlIEFQSS4gTGVhcm4gbW9yZSBhYm91dCB0aGUgW1F1ZXJ5IFBhcmFtZXRlcl0oaHR0cHM6Ly93ZWF0aGVyc3RhY2suY29tL2RvY3VtZW50YXRpb24jcXVlcnlfcGFyYW1ldGVyKS4NCiAgICAgICAgLSBuYW1lOiB1bml0cw0KICAgICAgICAgIGluOiBxdWVyeQ0KICAgICAgICAgIHNjaGVtYToNCiAgICAgICAgICAgIHR5cGU6IHN0cmluZw0KICAgICAgICAgICAgZW51bTogWyJtIiwgInMiLCAiZiJdDQogICAgICAgICAgZGVzY3JpcHRpb246IFVzZSB0aGlzIHBhcmFtZXRlciB0byBwYXNzIG9uZSBvZiB0aGUgdW5pdCBpZGVudGlmaWVycyBvdCB0aGUgQVBJIChtIGZvciBNZXRyaWMsIHMgZm9yIFNjaWVudGlmaWMsIGYgZm9yIEZhaHJlbmhlaXQpIExlYXJuIG1vcmUgYWJvdXQgdGhlIFtVbml0cyBQYXJhbWV0ZXJdKGh0dHBzOi8vd2VhdGhlcnN0YWNrLmNvbS9kb2N1bWVudGF0aW9uI3VuaXRzX3BhcmFtZXRlcikuDQogICAgICAgIC0gbmFtZTogbGFuZ3VhZ2UNCiAgICAgICAgICBpbjogcXVlcnkNCiAgICAgICAgICBzY2hlbWE6DQogICAgICAgICAgICB0eXBlOiBzdHJpbmcNCiAgICAgICAgICBkZXNjcmlwdGlvbjogVXNlIHRoaXMgcGFyYW1ldGVyIHRvIHNwZWNpZnkgeW91ciBwcmVmZXJyZWQgQVBJIHJlc3BvbnNlIGxhbmd1YWdlIHVzaW5nIGl0cyBJU08tY29kZS4gKERlZmF1bHQgdW5zZXQsIEVuZ2xpc2gpIExlYXJuIG1vcmUgYWJvdXQgdGhlIFtMYW5ndWFnZSBQYXJhbWV0ZXJdKGh0dHBzOi8vd2VhdGhlcnN0YWNrLmNvbS9kb2N1bWVudGF0aW9uI2xhbmd1YWdlX3BhcmFtZXRlcikuDQogICAgICAgIC0gbmFtZTogY2FsbGJhY2sNCiAgICAgICAgICBpbjogcXVlcnkNCiAgICAgICAgICBzY2hlbWE6DQogICAgICAgICAgICB0eXBlOiBzdHJpbmcNCiAgICAgICAgICBkZXNjcmlwdGlvbjogVXNlIHRoaXMgcGFyYW1ldGVyIHRvIHNwZWNpZnkgYSBKU09OUCBjYWxsYmFjayBmdW5jdGlvbiBuYW1lIHRvIHdyYXAgeW91ciBBUEkgcmVzcG9uc2UgaW4uIExlYXJuIG1vcmUgYWJvdXQgW0pTT05QIENhbGxiYWNrc10oaHR0cHM6Ly93ZWF0aGVyc3RhY2suY29tL2RvY3VtZW50YXRpb24janNvbnBfY2FsbGJhY2tzKS4NCiAgICAgIHJlc3BvbnNlczoNCiAgICAgICAgJzIwMCc6DQogICAgICAgICAgZGVzY3JpcHRpb246IFN1Y2Nlc3NmdWwgcmVzcG9uc2UNCiAgICAgICAgICBjb250ZW50Og0KICAgICAgICAgICAgYXBwbGljYXRpb24vanNvbjoNCiAgICAgICAgICAgICAgc2NoZW1hOg0KICAgICAgICAgICAgICAgICRyZWY6ICcjL2NvbXBvbmVudHMvc2NoZW1hcy9XZWF0aGVyJw0KDQogICAgICAgICc1MDAnOg0KICAgICAgICAgIGRlc2NyaXB0aW9uOiBJbnRlcm5hbCBTZXJ2ZXIgRXJyb3INCiAgICAgICAgICBjb250ZW50Og0KICAgICAgICAgICAgYXBwbGljYXRpb24vanNvbjoNCiAgICAgICAgICAgICAgc2NoZW1hOg0KICAgICAgICAgICAgICAgICRyZWY6ICcjL2NvbXBvbmVudHMvc2NoZW1hcy9XZWF0aGVyU3RhY2tFcnJvcicNCg0KY29tcG9uZW50czoNCiAgc2NoZW1hczoNCiAgICBXZWF0aGVyOg0KICAgICAgdHlwZTogb2JqZWN0DQogICAgICBwcm9wZXJ0aWVzOg0KICAgICAgICByZXF1ZXN0Og0KICAgICAgICAgICRyZWY6ICcjL2NvbXBvbmVudHMvc2NoZW1hcy9SZXF1ZXN0Jw0KICAgICAgICBsb2NhdGlvbjoNCiAgICAgICAgICAkcmVmOiAnIy9jb21wb25lbnRzL3NjaGVtYXMvTG9jYXRpb24nDQogICAgICAgIGN1cnJlbnQ6DQogICAgICAgICAgJHJlZjogJyMvY29tcG9uZW50cy9zY2hlbWFzL0N1cnJlbnQnDQogICAgUmVxdWVzdDoNCiAgICAgIHR5cGU6IG9iamVjdA0KICAgICAgcHJvcGVydGllczoNCiAgICAgICAgdHlwZToNCiAgICAgICAgICAkcmVmOiAnIy9jb21wb25lbnRzL3NjaGVtYXMvTG9jYXRpb25UeXBlRW51bScNCiAgICAgICAgcXVlcnk6DQogICAgICAgICAgdHlwZTogc3RyaW5nDQogICAgICAgICAgZGVzY3JpcHRpb246IFJldHVybnMgdGhlIGV4YWN0IGxvY2F0aW9uIGlkZW50aWZpZXIgcXVlcnkgdXNlZCBmb3IgdGhpcyByZXF1ZXN0Lg0KICAgICAgICBsYW5ndWFnZToNCiAgICAgICAgICB0eXBlOiBzdHJpbmcNCiAgICAgICAgICBkZXNjcmlwdGlvbjogUmV0dXJucyB0aGUgSVNPLUNvZGUgb2YgdGhlIGxhbmd1YWdlIHVzZWQgZm9yIHRoaXMgcmVxdWVzdC4NCiAgICAgICAgdW5pdDoNCiAgICAgICAgICAkcmVmOiAnIy9jb21wb25lbnRzL3NjaGVtYXMvVW5pdEVudW0nDQogICAgTG9jYXRpb25UeXBlRW51bToNCiAgICAgIHR5cGU6IHN0cmluZw0KICAgICAgZW51bTogWyAiQ2l0eSIsICJMYXRMb24iLCAiSVAiLCAiWmlwY29kZSIgXQ0KICAgICAgZGVzY3JpcHRpb246IFJldHVybnMgdGhlIHR5cGUgb2YgbG9jYXRpb24gbG9va3VwIHVzZWQgZm9yIHRoaXMgcmVxdWVzdC4NCiAgICBVbml0RW51bToNCiAgICAgIHR5cGU6IHN0cmluZw0KICAgICAgZW51bTogWyAibSIsICJzIiwgImYiIF0NCiAgICAgIGRlc2NyaXB0aW9uOiBSZXR1cm5zIHRoZSB1bml0IGlkZW50aWZpZXIgdXNlZCBmb3IgdGhpcyByZXF1ZXN0Lg0KICAgIExvY2F0aW9uOg0KICAgICAgdHlwZTogb2JqZWN0DQogICAgICBwcm9wZXJ0aWVzOg0KICAgICAgICBuYW1lOg0KICAgICAgICAgIHR5cGU6IHN0cmluZw0KICAgICAgICAgIGRlc2NyaXB0aW9uOiBSZXR1cm5zIHRoZSBuYW1lIG9mIHRoZSBsb2NhdGlvbiB1c2VkIGZvciB0aGlzIHJlcXVlc3QuDQogICAgICAgIGNvdW50cnk6DQogICAgICAgICAgdHlwZTogc3RyaW5nDQogICAgICAgICAgZGVzY3JpcHRpb246IFJldHVybnMgdGhlIGNvdW50cnkgbmFtZSBhc3NvY2lhdGVkIHdpdGggdGhlIGxvY2F0aW9uIHVzZWQgZm9yIHRoaXMgcmVxdWVzdC4NCiAgICAgICAgcmVnaW9uOg0KICAgICAgICAgIHR5cGU6IHN0cmluZw0KICAgICAgICAgIGRlc2NyaXB0aW9uOiBSZXR1cm5zIHRoZSByZWdpb24gbmFtZSBhc3NvY2lhdGVkIHdpdGggdGhlIGxvY2F0aW9uIHVzZWQgZm9yIHRoaXMgcmVxdWVzdC4NCiAgICAgICAgbGF0Og0KICAgICAgICAgIHR5cGU6IHN0cmluZw0KICAgICAgICAgIGRlc2NyaXB0aW9uOiBSZXR1cm5zIHRoZSBsYXRpdHVkZSBjb29yZGluYXRlIGFzc29jaWF0ZWQgd2l0aCB0aGUgbG9jYXRpb24gdXNlZCBmb3IgdGhpcyByZXF1ZXN0Lg0KICAgICAgICBsb246DQogICAgICAgICAgdHlwZTogc3RyaW5nDQogICAgICAgICAgZGVzY3JpcHRpb246IFJldHVybnMgdGhlIGxvbmdpdHVkZSBjb29yZGluYXRlIGFzc29jaWF0ZWQgd2l0aCB0aGUgbG9jYXRpb24gdXNlZCBmb3IgdGhpcyByZXF1ZXN0Lg0KICAgICAgICB0aW1lem9uZV9pZDoNCiAgICAgICAgICB0eXBlOiBzdHJpbmcNCiAgICAgICAgICBkZXNjcmlwdGlvbjogUmV0dXJucyB0aGUgdGltZXpvbmUgSUQgYXNzb2NpYXRlZCB3aXRoIHRoZSBsb2NhdGlvbiB1c2VkIGZvciB0aGlzIHJlcXVlc3QuDQogICAgICAgICAgZXhhbXBsZTogQW1lcmljYS9OZXdfWW9yaw0KICAgICAgICBsb2NhbHRpbWU6DQogICAgICAgICAgdHlwZTogc3RyaW5nDQogICAgICAgICAgZGVzY3JpcHRpb246IFJldHVybnMgdGhlIGxvY2FsIHRpbWUgb2YgdGhlIGxvY2F0aW9uIHVzZWQgZm9yIHRoaXMgcmVxdWVzdC4NCiAgICAgICAgICBleGFtcGxlOiAyMDE5LTA5LTA3IDA4OjE0DQogICAgICAgICAgeC1maWVsZC1leHRyYS1hbm5vdGF0aW9uOiAiQGNvbS5nb29nbGUuZ3Nvbi5hbm5vdGF0aW9ucy5TZXJpYWxpemVkTmFtZShcImxvY2FsdGltZVwiKSINCiAgICAgICAgbG9jYWx0aW1lX2Vwb2NoOg0KICAgICAgICAgIHR5cGU6IGludGVnZXINCiAgICAgICAgICBkZXNjcmlwdGlvbjogUmV0dXJucyB0aGUgbG9jYWwgdGltZSAoYXMgVU5JWCB0aW1lc3RhbXApIG9mIHRoZSBsb2NhdGlvbiB1c2VkIGZvciB0aGlzIHJlcXVlc3QuDQogICAgICAgICAgZXhhbXBsZTogMTU2Nzg0NDA0MA0KICAgICAgICB1dGNfb2Zmc2V0Og0KICAgICAgICAgIHR5cGU6IHN0cmluZw0KICAgICAgICAgIGRlc2NyaXB0aW9uOiBSZXR1cm5zIHRoZSBVVEMgb2Zmc2V0IChpbiBob3Vycykgb2YgdGhlIHRpbWV6b25lIGFzc29jaWF0ZWQgd2l0aCB0aGUgbG9jYXRpb24gdXNlZCBmb3IgdGhpcyByZXF1ZXN0Lg0KICAgICAgICAgIGV4YW1wbGU6IC00LjANCiAgICBDdXJyZW50Og0KICAgICAgdHlwZTogb2JqZWN0DQogICAgICBwcm9wZXJ0aWVzOg0KICAgICAgICBvYnNlcnZhdGlvbl90aW1lOg0KICAgICAgICAgIHR5cGU6IHN0cmluZw0KICAgICAgICAgIGRlc2NyaXB0aW9uOiBSZXR1cm5zIHRoZSBVVEMgdGltZSBmb3Igd2hlbiB0aGUgcmV0dXJuZWQgd2hldGhlciBkYXRhIHdhcyBjb2xsZWN0ZWQuDQogICAgICAgIHRlbXBlcmF0dXJlOg0KICAgICAgICAgIHR5cGU6IGludGVnZXINCiAgICAgICAgICBkZXNjcmlwdGlvbjogUmV0dXJucyB0aGUgdGVtcGVyYXR1cmUgaW4gdGhlIHNlbGVjdGVkIHVuaXQuIChEZWZhdWx0IENlbHNpdXMpDQogICAgICAgIHdlYXRoZXJfY29kZToNCiAgICAgICAgICB0eXBlOiBpbnRlZ2VyDQogICAgICAgICAgZGVzY3JpcHRpb246IFJldHVybnMgdGhlIHVuaXZlcnNhbCB3ZWF0aGVyIGNvbmRpdGlvbiBjb2RlIGFzc29jaWF0ZWQgd2l0aCB0aGUgY3VycmVudCB3ZWF0aGVyIGNvbmRpdGlvbi4gWW91IGNhbiBkb3dubG9hZCBhbGwgYXZhaWxhYmxlIHdlYXRoZXIgY29kZXMgdXNpbmcgdGhpcyBbbGlua10oaHR0cHM6Ly93ZWF0aGVyc3RhY2suY29tL3NpdGVfcmVzb3VyY2VzL3dlYXRoZXJzdGFjay13ZWF0aGVyLWNvbmRpdGlvbi1jb2Rlcy56aXApDQogICAgICAgIHdlYXRoZXJfaWNvbnM6DQogICAgICAgICAgdHlwZTogYXJyYXkNCiAgICAgICAgICBpdGVtczoNCiAgICAgICAgICAgIHR5cGU6IHN0cmluZw0KICAgICAgICAgIGRlc2NyaXB0aW9uOiBSZXR1cm5zIG9uZSBvciBtb3JlIFBORyB3ZWF0aGVyIGljb25zIGFzc29jaWF0ZWQgd2l0aCB0aGUgY3VycmVudCB3ZWF0aGVyIGNvbmRpdGlvbi4NCiAgICAgICAgd2VhdGhlcl9kZXNjcmlwdGlvbnM6DQogICAgICAgICAgdHlwZTogYXJyYXkNCiAgICAgICAgICBpdGVtczoNCiAgICAgICAgICAgIHR5cGU6IHN0cmluZw0KICAgICAgICAgIGRlc2NyaXB0aW9uOiBSZXR1cm5zIG9uZSBvciBtb3JlIHdlYXRoZXIgZGVzY3JpcHRpb24gdGV4dHMgYXNzb2NpYXRlZCB3aXRoIHRoZSBjdXJyZW50IHdlYXRoZXIgY29uZGl0aW9uLg0KICAgICAgICB3aW5kX3NwZWVkOg0KICAgICAgICAgIHR5cGU6IGludGVnZXINCiAgICAgICAgICBkZXNjcmlwdGlvbjogUmV0dXJucyB0aGUgd2luZCBzcGVlZCBpbiB0aGUgc2VsZWN0ZWQgdW5pdC4gKERlZmF1bHQga2lsb21ldGVycy9ob3VyKS4NCiAgICAgICAgd2luZF9kZWdyZWU6DQogICAgICAgICAgdHlwZTogaW50ZWdlcg0KICAgICAgICAgIGRlc2NyaXB0aW9uOiBSZXR1cm5zIHRoZSB3aW5kIGRlZ3JlZS4NCiAgICAgICAgd2luZF9kaXI6DQogICAgICAgICAgdHlwZTogc3RyaW5nDQogICAgICAgICAgZGVzY3JpcHRpb246IFJldHVybnMgdGhlIHdpbmQgZGlyZWN0aW9uLg0KICAgICAgICBwcmVzc3VyZToNCiAgICAgICAgICB0eXBlOiBpbnRlZ2VyDQogICAgICAgICAgZGVzY3JpcHRpb246IFJldHVybnMgdGhlIGFpciBwcmVzc3VyZSBpbiB0aGUgc2VsZWN0ZWQgdW5pdC4gKERlZmF1bHQgTUIgLSBtaWxsaWJhcikuDQogICAgICAgIHByZWNpcDoNCiAgICAgICAgICB0eXBlOiBudW1iZXINCiAgICAgICAgICBkZXNjcmlwdGlvbjogUmV0dXJucyB0aGUgcHJlY2lwaXRhdGlvbiBsZXZlbCBpbiB0aGUgc2VsZWN0ZWQgdW5pdC4gKERlZmF1bHQgTU0gLSBtaWxsaW1ldGVycykNCiAgICAgICAgICBmb3JtYXQ6IGZsb2F0DQogICAgICAgIGh1bWlkaXR5Og0KICAgICAgICAgIHR5cGU6IGludGVnZXINCiAgICAgICAgICBkZXNjcmlwdGlvbjogUmV0dXJucyB0aGUgYWlyIGh1bWlkaXR5IGxldmVsIGluIHBlcmNlbnRhZ2UuDQogICAgICAgIGNsb3VkY292ZXI6DQogICAgICAgICAgdHlwZTogaW50ZWdlcg0KICAgICAgICAgIGRlc2NyaXB0aW9uOiBSZXR1cm5zIHRoZSBjbG91ZCBjb3ZlciBsZXZlbCBpbiBwZXJjZW50YWdlLg0KICAgICAgICBmZWVsc2xpa2U6DQogICAgICAgICAgdHlwZTogaW50ZWdlcg0KICAgICAgICAgIGRlc2NyaXB0aW9uOiBSZXR1cm5zIHRoZSAiRmVlbHMgTGlrZSIgdGVtcGVyYXR1cmUgaW4gdGhlIHNlbGVjdGVkIHVuaXQuIChEZWZhdWx0IENlbHNpdXMpDQogICAgICAgIHV2X2luZGV4Og0KICAgICAgICAgIHR5cGU6IGludGVnZXINCiAgICAgICAgICBkZXNjcmlwdGlvbjogUmV0dXJucyB0aGUgVVYgaW5kZXggYXNzb2NpYXRlZCB3aXRoIHRoZSBjdXJyZW50IHdlYXRoZXIgY29uZGl0aW9uLg0KICAgICAgICB2aXNpYmlsaXR5Og0KICAgICAgICAgIHR5cGU6IGludGVnZXINCiAgICAgICAgICBkZXNjcmlwdGlvbjogUmV0dXJucyB0aGUgdmlzaWJpbGl0eSBsZXZlbCBpbiB0aGUgc2VsZWN0ZWQgdW5pdC4gKERlZmF1bHQga2lsb21ldGVycykNCiAgICBXZWF0aGVyU3RhY2tFcnJvcjoNCiAgICAgIHR5cGU6IG9iamVjdA0KICAgICAgcHJvcGVydGllczoNCiAgICAgICAgc3VjY2VzczoNCiAgICAgICAgICB0eXBlOiBib29sZWFuDQogICAgICAgIGVycm9yOg0KICAgICAgICAgIHR5cGU6IG9iamVjdA0KICAgICAgICAgIGRlc2NyaXB0aW9uOiBkZXRhaWxzIGFib3V0IHRoZSBlcnJvciB0aGF0IG9jY3VycmVkLg0KICAgICAgICAgIHByb3BlcnRpZXM6DQogICAgICAgICAgICBjb2RlOg0KICAgICAgICAgICAgICB0eXBlOiBpbnRlZ2VyDQogICAgICAgICAgICB0eXBlOg0KICAgICAgICAgICAgICB0eXBlOiBzdHJpbmcNCiAgICAgICAgICAgIGluZm86DQogICAgICAgICAgICAgIHR5cGU6IHN0cmluZw0K";

  private static Stream<Arguments> methodSourceProvider() {
    List<Arguments> argumentsList = new ArrayList<>();

    List<List<String>> consumerContractsList =
        List.of(
            List.of(),
            List.of(BASE_64_MEDIASTACK_YAML),
            List.of(BASE_64_MEDIASTACK_YAML, BASE_64_WEATHERSTACK_YAML));

    for (boolean allowCors : new boolean[] {true, false}) {
      for (boolean controllerAdvice : new boolean[] {true, false}) {
        for (boolean logFilter : new boolean[] {true, false}) {
          for (boolean loggingAspect : new boolean[] {true, false}) {
            Options options =
                new Options()
                    .allowCorsFromAnySource(allowCors)
                    .controllerAdvice(controllerAdvice)
                    .logFilter(logFilter)
                    .loggingAspect(loggingAspect);

            for (List<String> consumerContracts : consumerContractsList) {
              argumentsList.add(Arguments.of(options, consumerContracts));
            }
          }
        }
      }
    }

    return argumentsList.stream();
  }

  @ParameterizedTest
  @MethodSource("methodSourceProvider")
  @SneakyThrows
  void generatedZipContainsExpectedFiles(Options options, List<String> consumerContracts) {
    GenerateRequest request =
        new GenerateRequest()
            .contracts(
                new Contracts()
                    .providerContract(BASE_64_PETSORE_YAML)
                    .consumerContracts(consumerContracts))
            .projectMetadata(
                new ProjectMetadata()
                    .group(PROJECT_METADATA_GROUP)
                    .artifact(PROJECT_METADATA_ARTIFACT)
                    .description(PROJECT_METADATA_DESCRIPTION))
            .options(options);

    Map<String, Boolean> expectedEntryNames = buildExpectedEntryNamesMap(request);

    try (ZipInputStream zipInputStream =
        new ZipInputStream(
            new ByteArrayInputStream(generatePoofService.generateFiles(request).toByteArray()))) {

      ZipEntry entry;
      while (Objects.nonNull(entry = zipInputStream.getNextEntry())) {
        String entryName = entry.getName();
        checkFileIsExpected(expectedEntryNames, entryName);

        if (entryName.equals("pom.xml")) {
          pomXmlAssertions(request, readZipEntryContent(zipInputStream));
        } else if (entryName.equals("src/main/resources/application.yml")) {
          applicationYmlAssertions(readZipEntryContent(zipInputStream));
        } else if (entryName.endsWith(".java")) {
          javaFilesAssertions(request, entryName, readZipEntryContent(zipInputStream));
        } else {
          checkFileIsNotEmpty(entryName, readZipEntryContent(zipInputStream));
        }
        expectedEntryNames.put(entryName, true);
        zipInputStream.closeEntry();
      }

      checkAllExpectedFilesWereCopied(expectedEntryNames);
    }
  }

  private void checkAllExpectedFilesWereCopied(@NotNull Map<String, Boolean> expectedEntryNames) {
    expectedEntryNames.forEach(
        (entryName, isFound) -> assertTrue(isFound, entryName + " should exist"));
  }

  private void checkFileIsExpected(
      @NotNull Map<String, Boolean> expectedEntryNames, String entryName) {
    assertTrue(expectedEntryNames.containsKey(entryName), "Unexpected file: " + entryName);
  }

  private void checkFileIsNotEmpty(String entryName, @NotNull String fileContent) {
    assertFalse(fileContent.trim().isEmpty(), entryName + " should not be empty");
  }

  private void javaFilesAssertions(
      GenerateRequest request, @NotNull String entryName, @NotNull String javaFileContent) {
    assertTrue(
        javaFileContent.startsWith("package dev.pollito.poof"),
        entryName + " should start with 'package dev.pollito.poof'");

    if (entryName.equals("src/main/java/dev/pollito/poof/PoofApplication.java")) {
      mainJavaFileAssertions(javaFileContent);
    }
    if (entryName.equals("src/test/java/dev/pollito/poof/PoofApplicationTests.java")) {
      appTestFileAssertions(javaFileContent);
    }
    if (entryName.equals("src/main/java/dev/pollito/poof/aspect/LoggingAspect.java")) {
      aspectAssertions(request, javaFileContent);
    }
  }

  @NotNull
  private Map<String, Boolean> buildExpectedEntryNamesMap(@NotNull GenerateRequest request) {
    Map<String, Boolean> expectedEntryNames = new HashMap<>();
    expectedEntryNames.put(".mvn/wrapper/maven-wrapper.properties", false);
    expectedEntryNames.put("src/main/java/dev/pollito/poof/PoofApplication.java", false);
    expectedEntryNames.put("src/main/resources/application.yml", false);
    expectedEntryNames.put("src/main/resources/openapi/poof.yaml", false);
    expectedEntryNames.put("src/test/java/dev/pollito/poof/PoofApplicationTests.java", false);
    expectedEntryNames.put(".gitignore", false);
    expectedEntryNames.put("HELP.md", false);
    expectedEntryNames.put("mvnw", false);
    expectedEntryNames.put("mvnw.cmd", false);
    expectedEntryNames.put("pom.xml", false);

    if (request.getOptions().getLoggingAspect()) {
      expectedEntryNames.put("src/main/java/dev/pollito/poof/aspect/LoggingAspect.java", false);
    }
    if (request.getOptions().getLogFilter()) {
      expectedEntryNames.put("src/main/java/dev/pollito/poof/config/LogFilterConfig.java", false);
      expectedEntryNames.put("src/main/java/dev/pollito/poof/filter/LogFilter.java", false);
    }
    if (request.getOptions().getAllowCorsFromAnySource()) {
      expectedEntryNames.put("src/main/java/dev/pollito/poof/config/WebConfig.java", false);
    }
    if (request.getOptions().getControllerAdvice()) {
      expectedEntryNames.put(
          "src/main/java/dev/pollito/poof/controller/advice/GlobalControllerAdvice.java", false);
    }
    return expectedEntryNames;
  }

  private void aspectAssertions(@NotNull GenerateRequest request, String aspectContent) {
    if (request.getOptions().getLoggingAspect()) {
      assertNotNull(aspectContent, "LoggingAspect.java should exist");
      assertTrue(
          aspectContent.contains("public class LoggingAspect"),
          "LoggingAspect.java should contain the correct class name");
      assertTrue(
          aspectContent.contains(
              "@Pointcut(\"execution(public * dev.pollito.poof.controller..*.*(..))\")"),
          "LoggingAspect.java should contain the correct pointcut expression");
    } else {
      assertNull(aspectContent, "LoggingAspect.java should not exist");
    }
  }

  private void applicationYmlAssertions(String applicationYmlContent) {
    assertNotNull(applicationYmlContent, "application.yml content should not be null");
    assertTrue(
        applicationYmlContent.contains("name: poof"),
        "application.yml should contain the correct spring application name");
  }

  private void appTestFileAssertions(String appTestFileContent) {
    assertNotNull(appTestFileContent, "PoofApplicationTests.java content should not be null");
    assertTrue(
        appTestFileContent.contains("class PoofApplicationTests {"),
        "Main Java application test file should contain the correct class name");
  }

  private void mainJavaFileAssertions(String mainJavaAppFileContent) {
    assertNotNull(mainJavaAppFileContent, "PoofApplication.java content should not be null");
    assertTrue(
        mainJavaAppFileContent.contains("public class PoofApplication {"),
        "Main Java application file should contain the correct class name");
    assertTrue(
        mainJavaAppFileContent.contains("SpringApplication.run(PoofApplication.class, args);"),
        "Main Java application file should run the correct SpringApplication.run");
  }

  private void pomXmlAssertions(@NotNull GenerateRequest request, String pomXmlContent) {
    pomXmlBasicInfoAssertions(pomXmlContent);
    pomXmlAspectjAssertions(request, pomXmlContent);
    pomXmlProviderGenerationAssertions(pomXmlContent);
    pomXmlConsumerGenerationAssertions(request, pomXmlContent);
  }

  private void pomXmlConsumerGenerationAssertions(
      @NotNull GenerateRequest request, @NotNull String pomXmlContent) {
    List<String> dependencies =
        List.of(
            "<artifactId>javax.annotation-api</artifactId>",
            "<artifactId>feign-okhttp</artifactId>",
            "<artifactId>spring-cloud-starter-openfeign</artifactId>",
            "<artifactId>feign-jackson</artifactId>",
            "<artifactId>jsr305</artifactId>",
            "<artifactId>junit-jupiter-api</artifactId>",
            "<artifactId>feign-gson</artifactId>");
    String marker = "<!--consumer dependencies-->";
    boolean expected = !request.getContracts().getConsumerContracts().isEmpty();

    assertEquals(
        expected,
        pomXmlContent.contains(marker),
        "pom.xml should " + (expected ? "" : "not ") + "contain consumer dependencies comment");
    dependencies.forEach(
        dependency ->
            assertEquals(
                expected,
                pomXmlContent.contains(dependency),
                "pom.xml should " + (expected ? "" : "not ") + "contain " + dependency));
  }

  private void pomXmlProviderGenerationAssertions(@NotNull String pomXmlContent) {
    assertTrue(
        pomXmlContent.contains("<id>provider generation - poof.yaml</id>"),
        "pom.xml should contain the correct org.openapitools:openapi-generator-maven-plugin provider execution id");
    assertTrue(
        pomXmlContent.contains(
            "<inputSpec>${project.basedir}/src/main/resources/openapi/poof.yaml</inputSpec>"),
        "pom.xml should contain the correct org.openapitools:openapi-generator-maven-plugin provider execution configuration inputSpec");
    assertTrue(
        pomXmlContent.contains("<apiPackage>${project.groupId}.poof.api</apiPackage>"),
        "pom.xml should contain the correct org.openapitools:openapi-generator-maven-plugin provider execution configuration apiPackage");
    assertTrue(
        pomXmlContent.contains("<modelPackage>${project.groupId}.poof.model</modelPackage>"),
        "pom.xml should contain the correct org.openapitools:openapi-generator-maven-plugin provider execution configuration modelPackage");
  }

  private void pomXmlAspectjAssertions(
      @NotNull GenerateRequest request, @NotNull String pomXmlContent) {
    String aspectjArtifactId = "<artifactId>aspectjtools</artifactId>";
    String marker = "<!--aspectj-->";
    boolean expected = request.getOptions().getLoggingAspect();

    assertEquals(
        expected,
        pomXmlContent.contains(aspectjArtifactId),
        "pom.xml should " + (expected ? "" : "not ") + "contain artifactId org.aspectj");
    assertEquals(
        expected,
        pomXmlContent.contains(marker),
        "pom.xml should " + (expected ? "" : "not ") + "contain aspectj comment");
  }

  private void pomXmlBasicInfoAssertions(@NotNull String pomXmlContent) {
    assertTrue(
        pomXmlContent.contains("<groupId>dev.pollito</groupId>"),
        "pom.xml should contain the correct <groupId>");
    assertTrue(
        pomXmlContent.contains("<artifactId>poof</artifactId>"),
        "pom.xml should contain the correct <artifactId>");
    assertTrue(
        pomXmlContent.contains("<name>poof</name>"), "pom.xml should contain the correct <name>");
    assertTrue(
        pomXmlContent.contains(
            "<description>poof - Pollito Over Opinionated Framework</description>"),
        "pom.xml should contain the correct <description>");
  }

  @SneakyThrows
  private String readZipEntryContent(@NotNull InputStream inputStream) {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int length;
    while ((length = inputStream.read(buffer)) != -1) {
      byteArrayOutputStream.write(buffer, 0, length);
    }
    return byteArrayOutputStream.toString(StandardCharsets.UTF_8.name());
  }

  @Test
  void generatePoofThrowsException() {
    GenerateRequest generateRequest = new GenerateRequest();
    assertThrows(RuntimeException.class, () -> generatePoofService.generateFiles(generateRequest));
  }
}
