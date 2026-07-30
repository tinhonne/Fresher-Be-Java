package com.example.demo.specification;

import com.example.demo.dto.request.CustomerSearchRequest;
import com.example.demo.entity.Customer;
import com.example.demo.entity.CustomerType;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public class CustomerSpecification {

    private CustomerSpecification(){}

    private static Specification<Customer> like(String field,String value){
        return(root,query,cb) ->{
            if(value==null || value.isBlank()){
                return cb.conjunction();
            }
            String escaped = value.trim().toLowerCase(Locale.ROOT)
                    .replace("\\", "\\\\")
                    .replace("%", "\\%")
                    .replace("_", "\\_");
            return cb.like(cb.lower(root.get(field)), "%" + escaped + "%", '\\');
        };
    }
    /**
     * Matches customers whose names contain the supplied value, ignoring case.
     * A null or blank value does not restrict the query.
     *
     * @param name the optional name substring
     * @return the customer name specification
     */
    public static Specification<Customer> hasName(String name){
        return like("name",name);
    }

    /**
     * Matches customers whose identity numbers contain the supplied value, ignoring case.
     * A null or blank value does not restrict the query.
     *
     * @param identityNo the optional identity-number substring
     * @return the customer identity-number specification
     */
    public static Specification<Customer> hasIdentityNo(String identityNo){
        return like("identityNo",identityNo);
    }

    /**
     * Matches customers whose mobile numbers contain the supplied value, ignoring case.
     * A null or blank value does not restrict the query.
     *
     * @param mobile the optional mobile-number substring
     * @return the customer mobile specification
     */
    public static Specification<Customer> hasMobile(String mobile){
        return like("mobile",mobile);
    }

    private static Specification<Customer> hasStatus(Integer status){
        return(root, query, cb) -> status == null
                ? cb.conjunction()
                : cb.equal(root.get("status"),status);
    }

    private static Specification<Customer> hasCustomerType(CustomerType customerType){
        return(root,query,cb) -> customerType == null
                ? cb.conjunction()
                : cb.equal(root.get("customerType"),customerType);
    }

    /**
     * Combines the request's optional filters with logical AND. Text filters use
     * case-insensitive substring matching; status and customer type use exact matching.
     * Null or blank individual values do not restrict the query.
     *
     * @param request the non-null search criteria
     * @return the combined customer specification
     */
    public static Specification<Customer> filter(CustomerSearchRequest request){
        if (request == null) {
            return Specification.where(null);
        }
        return Specification.where(hasName(request.getName()))
                .and(hasIdentityNo(request.getIdentityNo()))
                .and(hasMobile(request.getMobile()))
                .and(hasStatus(request.getStatus()))
                .and(hasCustomerType(request.getCustomerType()));
    }
}
