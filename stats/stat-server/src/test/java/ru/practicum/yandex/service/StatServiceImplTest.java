package ru.practicum.yandex.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.yandex.model.EndpointHit;
import ru.practicum.yandex.model.ViewStats;
import ru.practicum.yandex.repository.StatRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatServiceImplTest {

    @Mock
    private StatRepository statRepository;

    @InjectMocks
    private StatServiceImpl statService;

    private EndpointHit endpointHit;

    private ViewStats viewStats;

    @BeforeEach
    void init() {
        endpointHit = EndpointHit.builder()
                .ip("127.0.0.1")
                .uri("/uri")
                .build();
        viewStats = ViewStats.builder()
                .app("app")
                .uri("/stats/uri")
                .hits(5L)
                .build();
    }

    @Test
    @DisplayName("Add method hit")
    void methodHit_shouldInvokeSaveOnce() {
        when(statRepository.save(endpointHit))
                .thenReturn(endpointHit);

        statService.methodHit(endpointHit);

        verify(statRepository, times(1)).save(endpointHit);
    }

    @Test
    @DisplayName("Get stats for unique ips and uri in")
    void viewStats_whenUniqueAndUriNotNull_shouldInvokeFindStatsFromListWithUniqueIps() {
        List<String> uri = List.of("/uri");
        Boolean unique = true;
        when(statRepository.findStatsFromUriListWithUniqueIps(any(), any(), eq(uri)))
                .thenReturn(List.of(viewStats));

        statService.viewStats(LocalDateTime.now(), LocalDateTime.now(), uri, unique);

        verify(statRepository, times(1)).findStatsFromUriListWithUniqueIps(any(), any(), eq(uri));
        verify(statRepository, never()).findStatsWithUniqueIps(any(), any());
        verify(statRepository, never()).findStats(any(), any());
        verify(statRepository, never()).findStatsFromUrlList(any(), any(), any());
    }

    @Test
    @DisplayName("Get stats for unique ips")
    void viewStats_whenUniqueAndUriNull_shouldInvokeFindStatsWithUniqueIps() {
        List<String> uri = null;
        Boolean unique = true;
        when(statRepository.findStatsWithUniqueIps(any(), any()))
                .thenReturn(List.of(viewStats));

        statService.viewStats(LocalDateTime.now(), LocalDateTime.now(), uri, unique);

        verify(statRepository, times(1)).findStatsWithUniqueIps(any(), any());
        verify(statRepository, never()).findStatsFromUriListWithUniqueIps(any(), any(), any());
        verify(statRepository, never()).findStats(any(), any());
        verify(statRepository, never()).findStatsFromUrlList(any(), any(), any());
    }

    @Test
    @DisplayName("Get stats for all ips and uri in")
    void viewStats_whenNotUniqueAndUriNotNull_shouldInvokeFindStatsWithUniqueIps() {
        List<String> uri = List.of("/uri");
        Boolean unique = false;
        when(statRepository.findStatsFromUrlList(any(), any(), eq(uri)))
                .thenReturn(List.of(viewStats));

        statService.viewStats(LocalDateTime.now(), LocalDateTime.now(), uri, unique);

        verify(statRepository, times(1)).findStatsFromUrlList(any(), any(), eq(uri));
        verify(statRepository, never()).findStatsWithUniqueIps(any(), any());
        verify(statRepository, never()).findStatsFromUriListWithUniqueIps(any(), any(), any());
        verify(statRepository, never()).findStats(any(), any());
    }

    @Test
    @DisplayName("Get stats for all ips")
    void viewStats_whenNotUniqueAndUriNull_shouldInvokeFindStatsWithUniqueIps() {
        List<String> uri = null;
        Boolean unique = false;
        when(statRepository.findStats(any(), any()))
                .thenReturn(List.of(viewStats));

        statService.viewStats(LocalDateTime.now(), LocalDateTime.now(), uri, unique);

        verify(statRepository, times(1)).findStats(any(), any());
        verify(statRepository, never()).findStatsFromUrlList(any(), any(), any());
        verify(statRepository, never()).findStatsWithUniqueIps(any(), any());
        verify(statRepository, never()).findStatsFromUriListWithUniqueIps(any(), any(), any());
    }

    @Test
    @DisplayName("Get stats for uri with unique ips")
    void viewUniqueIpStatsForUri_shouldInvokeFindUniqueIpStatsForUriOnce() {
        String uri = "/uri";
        when(statRepository.findStatsForUriWithUniqueIps(uri))
                .thenReturn(viewStats);

        statService.viewStatsForSingleUriWithUniqueIps(uri);

        verify(statRepository, times(1)).findStatsForUriWithUniqueIps(uri);
    }
}