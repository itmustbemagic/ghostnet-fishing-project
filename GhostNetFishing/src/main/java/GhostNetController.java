import jakarta.annotation.ManagedBean;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.ValidatorException;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.MatchMode;
import org.primefaces.model.SortMeta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Named
@ViewScoped
@ManagedBean
public class GhostNetController implements Serializable {
    @Inject
    CurrentUser currentUser;
    EntityManager _entityManager;

    private List<GhostNet> filteredGhostNets;

    private List<FilterMeta> filterBy;

    private String selectedGhostNetId;
    private String selectedStatusGhostNetId;
    private String selectedStatus;
    private List<String> statusOptions;

    public List<FilterMeta> getFilterBy() {
        return filterBy;
    }

    public void setFilterBy(List<FilterMeta> filterBy) { this.filterBy = filterBy; }

    public List<GhostNet> getFilteredGhostNets() {
        return filteredGhostNets;
    }

    public void setFilteredGhostNets(List<GhostNet> filteredGhostNets) { this.filteredGhostNets = filteredGhostNets; }


    public List<String> getStatusOptions() {
        return statusOptions;
    }

    public void setStatusOptions(List<String> statusOptions) {
        this.statusOptions = statusOptions;
    }

    public String getSelectedStatus() {
        return selectedStatus;
    }

    public void setSelectedStatus(String selectedStatus) {
        this.selectedStatus = selectedStatus;
    }

    public String getSelectedStatusGhostNetId() {
        return selectedStatusGhostNetId;
    }

    public void setSelectedStatusGhostNetId(String selectedStatusGhostNetId) {
        this.selectedStatusGhostNetId = selectedStatusGhostNetId;
    }

    public String getSelectedGhostNetId() {
        return selectedGhostNetId;
    }

    public void setSelectedGhostNetId(String selectedGhostNetId) {
        this.selectedGhostNetId = selectedGhostNetId;
    }

    public GhostNetController() {
        _entityManager = Persistence.createEntityManagerFactory("ghostnet").createEntityManager();
        statusOptions = new ArrayList<>();
        statusOptions.add("Bergung bevorstehend");
        statusOptions.add("Geborgen");
        statusOptions.add("Verschollen");
        selectedStatus = statusOptions.get(0);

        filterBy = new ArrayList<>();
        filterBy.add(FilterMeta.builder()
                .field("status")
                .filterValue("Gemeldet")
                .matchMode(MatchMode.EQUALS)
                .build());
        filterBy.add(FilterMeta.builder()
                .field("status")
                .filterValue("All")
                .matchMode(MatchMode.EQUALS)
                .build());
    }

    public Boolean isRecover() {
        return currentUser.getRecover();
    }

    public String getDisplayName(AppUser user, String userType) {
        if (user == null && Objects.equals(userType, "recover")) {
            return "Niemand zugewiesen";
        }

        if (Objects.equals(userType, "reporter")) {
            return user.getFirstName() + " " + user.getLastName();
        } else if (Objects.equals(userType, "recover")) {
            return user.getFirstName() + " " + user.getLastName() + "; " + user.getPhoneNumber();
        }

        return "Anonym";
    }


    private GhostNet getGhostNetById(String ghostNetId) {
        List<GhostNet> ghostNets = _entityManager.createQuery("SELECT p from GhostNet p WHERE p.id = :ghostNetId", GhostNet.class)
                .setParameter("ghostNetId", ghostNetId).getResultList();

        if (ghostNets.isEmpty()) {
            return null;
        }
        return ghostNets.get(0);
    }

    private Boolean isGhostNetAlreadyAssigned(String ghostNetId) {
        List<GhostNet> ghostNets = _entityManager.createQuery("SELECT p from GhostNet p WHERE p.id = :ghostNetId AND p.recoverUser != null", GhostNet.class)
                .setParameter("ghostNetId", ghostNetId).getResultList();
        return !ghostNets.isEmpty();
    }

    private Boolean isUserAssigned(String ghostNetId, AppUser userId) {
        System.out.println(userId);
        List<GhostNet> ghostNets = _entityManager.createQuery("SELECT p from GhostNet p WHERE p.id = :ghostNetId AND p.recoverUser = :userId", GhostNet.class)
                .setParameter("ghostNetId", ghostNetId).setParameter("userId", userId).getResultList();
        return !ghostNets.isEmpty();
    }

    public void validateGhostNetId(FacesContext context, UIComponent component, Object value) {
        String ghostNetId = (String) value;
        if (getGhostNetById(ghostNetId) == null) {
            throw new ValidatorException(new FacesMessage("Das Geisternetz mit der eingegebenen ID wurde nicht gefunden!"));
        }
        if (isGhostNetAlreadyAssigned(ghostNetId)) {
            throw new ValidatorException(new FacesMessage("Das Geisternetz mit der eingegebene ID ist bereits jemandem zugewiesen!"));
        }
    }

    public void validateStatusGhostNetId(FacesContext context, UIComponent component, Object value) {
        String ghostNetId = (String) value;
        if (getGhostNetById(ghostNetId) == null) {
            throw new ValidatorException(new FacesMessage("Das Geisternetz mit der eingegebenen ID wurde nicht gefunden!"));
        }
        if (!isUserAssigned(ghostNetId, currentUser.getUser())) {
            throw new ValidatorException(new FacesMessage("Das Geisternetz mit der eingegebenen ID ist jemand anderem zugewiesen!"));
        }
    }

    public void assignToGhostNet() {
        try {
            EntityTransaction transaction = _entityManager.getTransaction();
            if (!transaction.isActive()) {
                transaction.begin();
            }
            GhostNet ghostNet = getGhostNetById(selectedGhostNetId);
            GhostNet mergedGhostnet = _entityManager.merge(ghostNet);
            mergedGhostnet.setRecoverUser(currentUser.getUser());
            mergedGhostnet.setStatus("Bergung bevorstehend");
            transaction.commit();
            selectedGhostNetId = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void changeGhostNetStatus() {
        try {
            EntityTransaction transaction = _entityManager.getTransaction();
            if (!transaction.isActive()) {
                transaction.begin();
            }
            GhostNet ghostNet = getGhostNetById(selectedStatusGhostNetId);
            GhostNet mergedGhostnet = _entityManager.merge(ghostNet);
            mergedGhostnet.setStatus(selectedStatus);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reportGhostNet(CurrentUser user, String latitude, String longitude, String length, String width) {
        EntityTransaction transaction = _entityManager.getTransaction();
        transaction.begin();
        _entityManager.persist(new GhostNet(latitude, longitude, length, width));
        transaction.commit();
    }

    private List<GhostNet> readData() {
        return _entityManager.createQuery("SELECT p from GhostNet p").getResultList();
    }

    private long readDataCount() {
        return _entityManager.createQuery("SELECT p FROM GhostNet p").getResultStream().count();
    }

    private List<GhostNet> readData(int first, int maxCount) {
        return _entityManager.createQuery("SELECT p from GhostNet p").setFirstResult(first).setMaxResults(maxCount).getResultList();

    }

    private LazyDataModel<GhostNet> readDataLazyModel() {
        return new LazyDataModel<GhostNet>() {
            @Override
            public int count(Map<String, FilterMeta> map) {
                return (int) readDataCount();
            }

            @Override
            public List<GhostNet> load(int i, int i1, Map<String, SortMeta> map, Map<String, FilterMeta> map1) {
                if (Objects.equals(String.valueOf(map1.get("status").getFilterValue()), "Gemeldet")) {
                    return _entityManager.createQuery("SELECT p from GhostNet p WHERE p.status = \"Gemeldet\"").setFirstResult(i).setMaxResults(i1).getResultList();
                }
                return _entityManager.createQuery("SELECT p from GhostNet p").setFirstResult(i).setMaxResults(i1).getResultList();
            }
        };
    }

    public LazyDataModel<GhostNet> getLazyModel() {
        return readDataLazyModel();
    }
}