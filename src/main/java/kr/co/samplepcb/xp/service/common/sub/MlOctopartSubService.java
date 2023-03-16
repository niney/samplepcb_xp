package kr.co.samplepcb.xp.service.common.sub;

import coolib.common.CCObjectResult;
import coolib.common.CCResult;
import kr.co.samplepcb.xp.config.ApplicationProperties;
import kr.co.samplepcb.xp.domain.NotOctopartForSearch;
import kr.co.samplepcb.xp.domain.OctopartForSearch;
import kr.co.samplepcb.xp.pojo.MlOctopart;
import kr.co.samplepcb.xp.pojo.OctopartVM;
import kr.co.samplepcb.xp.repository.NotOctopartSearchRepository;
import kr.co.samplepcb.xp.repository.OctopartSearchRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class MlOctopartSubService {

    private final ApplicationProperties applicationProperties;
    private final OctopartSearchRepository octopartSearchRepository;
    private final NotOctopartSearchRepository notOctopartSearchRepository;

    public MlOctopartSubService(ApplicationProperties applicationProperties, OctopartSearchRepository octopartSearchRepository, NotOctopartSearchRepository notOctopartSearchRepository) {
        this.applicationProperties = applicationProperties;
        this.octopartSearchRepository = octopartSearchRepository;
        this.notOctopartSearchRepository = notOctopartSearchRepository;
    }

    public CCResult indexing(OctopartForSearch octopartForSearch) {
        return this.indexing(octopartForSearch, 0);
    }
    private CCResult indexing(OctopartForSearch octopartForSearch, int tryCnt) {
        if (StringUtils.isNotEmpty(octopartForSearch.getMpn())) {
            octopartForSearch.setMpn(
                    octopartForSearch.getMpn()
                            .replace("\"", "")
                            .replace("'", "")
                            .replaceAll("\\p{Cntrl}", "")
            );
        }
        List<OctopartForSearch> findOctopartForSearches = this.octopartSearchRepository.findByMpn(octopartForSearch.getMpn());
        if (findOctopartForSearches.size() != 0) {
            // octopart 존재
            OctopartForSearch findOctopart = findOctopartForSearches.get(0);
            OctopartVM octopartVM = new OctopartVM();
            BeanUtils.copyProperties(findOctopart, octopartVM);
            octopartVM.setExist(true);
            return CCObjectResult.setSimpleData(octopartVM);
        }
        List<NotOctopartForSearch> findNotOctopartForSearches = this.notOctopartSearchRepository.findByMpn(octopartForSearch.getMpn());
        if (findNotOctopartForSearches.size() != 0) {
            // octopart 비존재
            return CCObjectResult.dataNotFound();
        }

        String serverUrl = this.applicationProperties.getMlServer().getServerUrl();
        try {
            MlOctopart octopart = WebClient.create(serverUrl + "/api/searchPartsMpn?q=" + octopartForSearch.getMpn())
                    .get()
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(MlOctopart.class)
                    .block();
            if (octopart != null && octopart.getSame() != null && octopart.getSame()) {
                OctopartVM octopartVM = new OctopartVM();
                BeanUtils.copyProperties(this.octopartSearchRepository.save(octopartForSearch), octopartVM);
                octopartVM.setExist(false);
                return CCObjectResult.setSimpleData(octopartVM);
            }
        } catch (Exception e) {
            if (tryCnt == 3) {
                throw new RuntimeException(e.getMessage());
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            tryCnt++;
            return this.indexing(octopartForSearch, tryCnt);
        }

        // 존재 하지 않는걸로 등록
        NotOctopartForSearch notOctopartForSearch = new NotOctopartForSearch();
        BeanUtils.copyProperties(octopartForSearch, notOctopartForSearch);
        this.notOctopartSearchRepository.save(notOctopartForSearch);
        return CCResult.dataNotFound();
    }
}
