Feature: Admin Manage Restaurant

  Scenario: Successful Addition of a Restaurant
    Given I am logged in as an admin
    When I attempt to add a restaurant with name "Restaurant A" and password "password123"
    Then the restaurant "Restaurant A" should be added successfully

  Scenario: Unauthorized Addition of a Restaurant
    Given I not logged in as an admin
    When I attempt to add a restaurant with name "Restaurant B" and password "password456"
    Then I should receive an unauthorized operation message

  Scenario: Successful Removal of a Restaurant
    Given I am logged in as an admin
    And there is a restaurant with id 1
    When I attempt to remove the restaurant with id 1
    Then the restaurant with id 1 should be removed successfully

  Scenario: Unauthorized Removal of a Restaurant
    Given I not logged in as an admin
    When I attempt to remove the restaurant with id 2
    Then I should receive an unauthorized operation message