import { useState, useEffect, useCallback } from 'react'
import './App.css'

const API_URL = 'http://localhost:8081/api/services'
const CUSTOMER_API_URL = 'http://localhost:8081/api/customers'

function sortServicesById(services) {
  return [...services].sort((a, b) => a.id - b.id)
}

function App() {
  // Services data and API states
  const [services, setServices] = useState([])
  const [loading, setLoading] = useState(true)
  const [apiError, setApiError] = useState('')

  // Navigation
  const [activePage, setActivePage] = useState('services')

  // Search
  const [search, setSearch] = useState('')

  // Customer management state (kept separate from Services state)
  const [customers, setCustomers] = useState([])
  const [customersLoading, setCustomersLoading] = useState(false)
  const [customersError, setCustomersError] = useState('')
  const [customerSearch, setCustomerSearch] = useState('')
  const [customerSuccessMessage, setCustomerSuccessMessage] = useState('')
  const [showCustomerForm, setShowCustomerForm] = useState(false)
  const [editingCustomer, setEditingCustomer] = useState(null)
  const [customerFullName, setCustomerFullName] = useState('')
  const [customerPhoneNumber, setCustomerPhoneNumber] = useState('')
  const [customerEmail, setCustomerEmail] = useState('')
  const [customerFormError, setCustomerFormError] = useState('')
  const [customerSaving, setCustomerSaving] = useState(false)
  const [deactivatingCustomerId, setDeactivatingCustomerId] = useState(null)

  // Add / Edit form
  const [showForm, setShowForm] = useState(false)
  const [editingService, setEditingService] = useState(null)

  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [price, setPrice] = useState('')
  const [formError, setFormError] = useState('')
  const [successMessage, setSuccessMessage] = useState('')
  const [saving, setSaving] = useState(false)

  // Load services from the backend.
  // This function is also used by the Retry button.
  const loadServices = useCallback(async () => {
    try {
      setLoading(true)
      setApiError('')

      const response = await fetch(API_URL)

      if (!response.ok) {
        throw new Error('Failed to load laundry services.')
      }

      const data = await response.json()

      if (!Array.isArray(data)) {
        throw new Error('The server returned an invalid services response.')
      }

      setServices(sortServicesById(data))
    } catch (error) {
      setApiError(
          error instanceof Error
              ? error.message
              : 'An unexpected error occurred while loading services.'
      )
    } finally {
      setLoading(false)
    }
  }, [])

  // Load services when the component first mounts.
  useEffect(() => {
    loadServices()
  }, [loadServices])

  // Filter services by name.
  const filteredServices = services.filter((service) =>
      String(service.name ?? '')
          .toLowerCase()
          .includes(search.trim().toLowerCase())
  )

  // Load customers from the backend.
  const loadCustomers = useCallback(async () => {
    try {
      setCustomersLoading(true)
      setCustomersError('')

      const response = await fetch(CUSTOMER_API_URL)
      if (!response.ok) {
        const errorBody = await response.json().catch(() => null)
        throw new Error(errorBody?.message || 'Failed to load customers.')
      }

      const data = await response.json()
      if (!Array.isArray(data)) {
        throw new Error('The server returned an invalid customers response.')
      }

      setCustomers([...data].sort((a, b) => a.id - b.id))
    } catch (error) {
      setCustomersError(
          error instanceof Error ? error.message : 'An unexpected error occurred while loading customers.'
      )
    } finally {
      setCustomersLoading(false)
    }
  }, [])

  useEffect(() => {
    if (activePage === 'customers') {
      loadCustomers()
    }
  }, [activePage, loadCustomers])

  const filteredCustomers = customers.filter((customer) => {
    const query = customerSearch.trim().toLowerCase()
    return (
        String(customer.fullName ?? '').toLowerCase().includes(query) ||
        String(customer.phoneNumber ?? '').toLowerCase().includes(query)
    )
  })

  function openAddCustomerForm() {
    setEditingCustomer(null)
    setCustomerFullName('')
    setCustomerPhoneNumber('')
    setCustomerEmail('')
    setCustomerFormError('')
    setCustomersError('')
    setCustomerSuccessMessage('')
    setShowCustomerForm(true)
  }

  function openEditCustomerForm(customer) {
    setEditingCustomer(customer)
    setCustomerFullName(customer.fullName ?? '')
    setCustomerPhoneNumber(customer.phoneNumber ?? '')
    setCustomerEmail(customer.email ?? '')
    setCustomerFormError('')
    setCustomersError('')
    setCustomerSuccessMessage('')
    setShowCustomerForm(true)
  }

  function closeCustomerForm() {
    if (customerSaving) return
    setShowCustomerForm(false)
    setEditingCustomer(null)
    setCustomerFormError('')
  }

  async function handleCustomerSubmit(event) {
    event.preventDefault()
    if (customerSaving) return

    setCustomerFormError('')
    setCustomersError('')
    setCustomerSuccessMessage('')

    const trimmedName = customerFullName.trim()
    const trimmedPhone = customerPhoneNumber.trim()
    const trimmedEmail = customerEmail.trim()

    if (!trimmedName) {
      setCustomerFormError('Please enter the customer name.')
      return
    }
    if (trimmedName.length < 2) {
      setCustomerFormError('Customer name must contain at least 2 characters.')
      return
    }
    if (!trimmedPhone) {
      setCustomerFormError('Please enter a phone number.')
      return
    }
    if (!/^[+]?[-\d()\s]{7,20}$/.test(trimmedPhone)) {
      setCustomerFormError('Please enter a valid phone number.')
      return
    }
    if (!trimmedEmail) {
      setCustomerFormError('Please enter an email address.')
      return
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(trimmedEmail)) {
      setCustomerFormError('Please enter a valid email address.')
      return
    }

    const requestBody = {
      fullName: trimmedName,
      phoneNumber: trimmedPhone,
      email: trimmedEmail,
    }
    const isEditing = editingCustomer !== null
    const requestUrl = isEditing
        ? `${CUSTOMER_API_URL}/${editingCustomer.id}`
        : CUSTOMER_API_URL

    setCustomerSaving(true)
    try {
      const response = await fetch(requestUrl, {
        method: isEditing ? 'PUT' : 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestBody),
      })

      if (!response.ok) {
        const errorBody = await response.json().catch(() => null)
        throw new Error(
            errorBody?.message ||
            errorBody?.detail ||
            `Could not ${isEditing ? 'update' : 'save'} the customer. Please check the details and try again.`
        )
      }

      const savedCustomer = await response.json()
      setCustomers((current) =>
          [...current.filter((customer) => customer.id !== savedCustomer.id), savedCustomer]
              .sort((a, b) => a.id - b.id)
      )
      setCustomerSuccessMessage(
          isEditing ? 'Customer updated successfully.' : 'Customer created successfully.'
      )
      setShowCustomerForm(false)
      setEditingCustomer(null)
      setCustomerSearch('')
      setCustomerFullName('')
      setCustomerPhoneNumber('')
      setCustomerEmail('')
    } catch (error) {
      setCustomerFormError(
          error instanceof Error ? error.message : 'An unexpected error occurred while saving the customer.'
      )
    } finally {
      setCustomerSaving(false)
    }
  }

  async function handleDeactivateCustomer(customer) {
    if (!customer.active) return

    const confirmed = window.confirm(
        `Are you sure you want to deactivate ${customer.fullName}? This will mark the customer as inactive.`
    )
    if (!confirmed) return

    setCustomersError('')
    setCustomerSuccessMessage('')
    setDeactivatingCustomerId(customer.id)
    try {
      const response = await fetch(`${CUSTOMER_API_URL}/${customer.id}`, {
        method: 'DELETE',
      })

      if (!response.ok) {
        const errorBody = await response.json().catch(() => null)
        throw new Error(errorBody?.message || 'Could not deactivate the customer.')
      }

      // DELETE returns 204 No Content, so do not try to parse a response body.
      setCustomers((current) =>
          current.map((item) => item.id === customer.id ? { ...item, active: false } : item)
      )
      setCustomerSuccessMessage(`${customer.fullName} was deactivated successfully.`)
    } catch (error) {
      setCustomersError(
          error instanceof Error ? error.message : 'An unexpected error occurred while deactivating the customer.'
      )
    } finally {
      setDeactivatingCustomerId(null)
    }
  }

  // Open the Add Service form.
  function openAddForm() {
    setEditingService(null)
    setName('')
    setDescription('')
    setPrice('')
    setFormError('')
    setApiError('')
    setSuccessMessage('')
    setShowForm(true)
  }

  // Open the Edit Service form.
  function openEditForm(service) {
    setEditingService(service)
    setName(service.name)
    setDescription(service.description || '')
    setPrice(String(service.price))
    setFormError('')
    setApiError('')
    setSuccessMessage('')
    setShowForm(true)
  }

  // Close the form.
  function closeForm() {
    if (saving) return

    setShowForm(false)
    setEditingService(null)
    setFormError('')
  }

  // Create or update a service.
  async function handleSubmit(event) {
    event.preventDefault()

    if (saving) return

    setFormError('')
    setApiError('')
    setSuccessMessage('')

    // Validate service name.
    if (!name.trim()) {
      setFormError('Please enter a service name.')
      return
    }

    // Validate price.
    if (
        price.trim() === '' ||
        !Number.isFinite(Number(price)) ||
        Number(price) <= 0
    ) {
      setFormError('Please enter a valid price greater than zero.')
      return
    }

    const requestBody = {
      name: name.trim(),
      description: description.trim(),
      price: Number(price),
    }

    const isEditing = editingService !== null

    const requestUrl = isEditing
        ? `${API_URL}/${editingService.id}`
        : API_URL

    const requestMethod = isEditing ? 'PUT' : 'POST'

    setSaving(true)

    try {
      const response = await fetch(requestUrl, {
        method: requestMethod,
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestBody),
      })

      if (!response.ok) {
        const errorBody = await response.json().catch(() => null)

        throw new Error(
            errorBody?.message ||
            `Could not ${isEditing ? 'update' : 'save'} the service.`
        )
      }

      const savedService = await response.json()

      if (isEditing) {
        setServices((currentServices) =>
            sortServicesById(
                currentServices.map((service) =>
                    service.id === savedService.id ? savedService : service
                )
            )
        )
      } else {
        setServices((currentServices) =>
            sortServicesById([...currentServices, savedService])
        )
      }

      setSuccessMessage(
          isEditing
              ? 'Service updated successfully.'
              : 'Service created successfully.'
      )

      setShowForm(false)
      setEditingService(null)
      setSearch('')
      setName('')
      setDescription('')
      setPrice('')
      setFormError('')
    } catch (error) {
      setFormError(
          error instanceof Error
              ? error.message
              : 'An unexpected error occurred while saving the service.'
      )
    } finally {
      setSaving(false)
    }
  }

  // Delete a service.
  async function handleDelete(service) {
    const confirmed = window.confirm(
        `Are you sure you want to delete "${service.name}"?`
    )

    if (!confirmed) return

    setApiError('')
    setSuccessMessage('')

    try {
      const response = await fetch(`${API_URL}/${service.id}`, {
        method: 'DELETE',
      })

      if (!response.ok) {
        const errorBody = await response.json().catch(() => null)

        throw new Error(
            errorBody?.message || 'Could not delete the service.'
        )
      }

      setServices((currentServices) =>
          currentServices.filter((item) => item.id !== service.id)
      )

      setSuccessMessage('Service deleted successfully.')
    } catch (error) {
      setApiError(
          error instanceof Error
              ? error.message
              : 'An unexpected error occurred while deleting the service.'
      )
    }
  }

  const pageTitles = {
    dashboard: 'Dashboard',
    services: 'Services',
    customers: 'Customers',
    orders: 'Orders',
  }

  return (
      <div className="app">
        <aside className="sidebar">
          <h2 className="brand">FreshFold</h2>
          <p className="brand-subtitle">Laundry Management</p>

          <nav className="navigation">
            {Object.entries(pageTitles).map(([page, title]) => (
                <a
                    key={page}
                    href={`#${page}`}
                    className={activePage === page ? 'active' : ''}
                    aria-current={activePage === page ? 'page' : undefined}
                    onClick={(event) => {
                      event.preventDefault()
                      setActivePage(page)
                      window.history.replaceState(null, '', `#${page}`)
                    }}
                >
                  {title}
                </a>
            ))}
          </nav>
        </aside>

        <main className="main-content">
          <header className="topbar">
            <span>Management / {pageTitles[activePage]}</span>
            <span className="user-label">Admin</span>
          </header>

          <section className="page-content">
            {activePage === 'services' && (
                <>
                  <div className="page-heading">
                    <div>
                      <h1>Laundry Services</h1>
                      <p>Manage your laundry services and pricing.</p>
                    </div>

                    <button
                        type="button"
                        className="primary-button"
                        onClick={openAddForm}
                        disabled={loading || Boolean(apiError)}
                    >
                      + Add Service
                    </button>
                  </div>

                  <div className="summary-card">
                    <span>Total Services</span>
                    <strong>{services.length}</strong>
                    <small>Services in this demo</small>
                  </div>

                  {loading && (
                      <div className="state-message" role="status">
                        <p>Loading services...</p>
                      </div>
                  )}

                  {!loading && apiError && (
                      <div className="state-message error-state" role="alert">
                        <p>{apiError}</p>

                        <button
                            type="button"
                            className="primary-button"
                            onClick={loadServices}
                        >
                          Retry
                        </button>
                      </div>
                  )}

                  {!loading && !apiError && (
                      <>
                        {successMessage && (
                            <p role="status" className="success-message">
                              {successMessage}
                            </p>
                        )}

                        <section className="services-card">
                          <div className="services-heading">
                            <h2>All Services</h2>

                            <input
                                type="text"
                                placeholder="Search services..."
                                value={search}
                                onChange={(event) =>
                                    setSearch(event.target.value)
                                }
                                aria-label="Search services by name"
                            />
                          </div>

                          {services.length === 0 ? (
                              <div className="state-message">
                                <p>No services have been added yet.</p>
                                <p>
                                  Use Add Service to create your first laundry
                                  service.
                                </p>
                              </div>
                          ) : filteredServices.length === 0 ? (
                              <div className="state-message">
                                <p>No services match "{search}".</p>

                                <button
                                    type="button"
                                    className="secondary-button"
                                    onClick={() => setSearch('')}
                                >
                                  Clear Search
                                </button>
                              </div>
                          ) : (
                              <div className="table-wrapper">
                                <table>
                                  <thead>
                                  <tr>
                                    <th>ID</th>
                                    <th>Service Name</th>
                                    <th>Description</th>
                                    <th>Price</th>
                                    <th>Actions</th>
                                  </tr>
                                  </thead>

                                  <tbody>
                                  {filteredServices.map((service) => (
                                      <tr key={service.id}>
                                        <td>{service.id}</td>

                                        <td className="service-name">
                                          {service.name}
                                        </td>

                                        <td>{service.description || '—'}</td>

                                        <td>
                                          ₹{Number(service.price).toFixed(2)}
                                        </td>

                                        <td>
                                          <button
                                              type="button"
                                              className="secondary-button"
                                              onClick={() =>
                                                  openEditForm(service)
                                              }
                                          >
                                            Edit
                                          </button>

                                          <button
                                              type="button"
                                              className="secondary-button"
                                              onClick={() =>
                                                  handleDelete(service)
                                              }
                                              style={{ marginLeft: '8px' }}
                                          >
                                            Delete
                                          </button>
                                        </td>
                                      </tr>
                                  ))}
                                  </tbody>
                                </table>
                              </div>
                          )}
                        </section>
                      </>
                  )}
                </>
            )}

            {activePage === 'dashboard' && (
                <>
                  <div className="page-heading">
                    <div>
                      <h1>Dashboard</h1>
                      <p>
                        Welcome to your laundry management workspace.
                      </p>
                    </div>
                  </div>

                  <div className="summary-card">
                    <span>Total Services</span>
                    <strong>{services.length}</strong>
                    <small>Services currently in this demo</small>
                  </div>

                  <section className="services-card">
                    <h2>Quick overview</h2>
                    <p>
                      Use the Services menu to manage your laundry service list.
                    </p>

                    <button
                        type="button"
                        className="primary-button"
                        onClick={() => setActivePage('services')}
                    >
                      View Services
                    </button>
                  </section>
                </>
            )}

            {activePage === 'customers' && (
                <>
                  <div className="page-heading">
                    <div>
                      <h1>Customer Management</h1>
                      <p>Manage customer details and account status.</p>
                    </div>
                    <button
                        type="button"
                        className="primary-button"
                        onClick={openAddCustomerForm}
                        disabled={customersLoading || Boolean(customersError)}
                    >
                      + Add Customer
                    </button>
                  </div>

                  <div className="customer-summary-grid">
                    <div className="summary-card">
                      <span>Total Customers</span>
                      <strong>{customers.length}</strong>
                      <small>All registered customers</small>
                    </div>
                    <div className="summary-card">
                      <span>Active Customers</span>
                      <strong>{customers.filter((customer) => customer.active).length}</strong>
                      <small>Currently active</small>
                    </div>
                    <div className="summary-card">
                      <span>Inactive Customers</span>
                      <strong>{customers.filter((customer) => !customer.active).length}</strong>
                      <small>Deactivated accounts</small>
                    </div>
                  </div>

                  {customerSuccessMessage && !customersLoading && (
                      <p role="status" className="success-message">{customerSuccessMessage}</p>
                  )}

                  {customersLoading && (
                      <div className="state-message" role="status"><p>Loading customers...</p></div>
                  )}

                  {!customersLoading && customersError && (
                      <div className="state-message error-state" role="alert">
                        <p>{customersError}</p>
                        <button type="button" className="primary-button" onClick={loadCustomers}>Retry</button>
                      </div>
                  )}

                  {!customersLoading && !customersError && (
                      <section className="services-card">
                        <div className="services-heading">
                          <h2>All Customers</h2>
                          <input
                              type="search"
                              placeholder="Search name or phone..."
                              value={customerSearch}
                              onChange={(event) => setCustomerSearch(event.target.value)}
                              aria-label="Search customers by name or phone number"
                          />
                        </div>

                        {customers.length === 0 ? (
                            <div className="state-message">
                              <p>No customers have been added yet.</p>
                              <p>Use Add Customer to register your first customer.</p>
                            </div>
                        ) : filteredCustomers.length === 0 ? (
                            <div className="state-message">
                              <p>No customers match “{customerSearch}”.</p>
                              <button type="button" className="secondary-button" onClick={() => setCustomerSearch('')}>
                                Clear Search
                              </button>
                            </div>
                        ) : (
                            <div className="table-wrapper">
                              <table className="customers-table">
                                <thead>
                                <tr>
                                  <th>ID</th>
                                  <th>Customer Name</th>
                                  <th>Phone Number</th>
                                  <th>Email</th>
                                  <th>Status</th>
                                  <th>Actions</th>
                                </tr>
                                </thead>
                                <tbody>
                                {filteredCustomers.map((customer) => (
                                    <tr key={customer.id}>
                                      <td>{customer.id}</td>
                                      <td className="service-name">{customer.fullName}</td>
                                      <td>{customer.phoneNumber}</td>
                                      <td>{customer.email}</td>
                                      <td>
                                  <span className={`status-badge ${customer.active ? 'status-active' : 'status-inactive'}`}>
                                    {customer.active ? 'Active' : 'Inactive'}
                                  </span>
                                      </td>
                                      <td className="customer-actions">
                                        <button
                                            type="button"
                                            className="secondary-button"
                                            onClick={() => openEditCustomerForm(customer)}
                                        >
                                          Edit
                                        </button>
                                        {customer.active && (
                                            <button
                                                type="button"
                                                className="danger-button"
                                                onClick={() => handleDeactivateCustomer(customer)}
                                                disabled={deactivatingCustomerId === customer.id}
                                            >
                                              {deactivatingCustomerId === customer.id ? 'Deactivating...' : 'Deactivate'}
                                            </button>
                                        )}
                                      </td>
                                    </tr>
                                ))}
                                </tbody>
                              </table>
                            </div>
                        )}
                      </section>
                  )}
                </>
            )}

            {activePage === 'orders' && (
                <section className="services-card">
                  <h1>Orders</h1>
                  <p>Order management will be implemented in the next phase.</p>
                </section>
            )}
          </section>
        </main>

        {showForm && (
            <div className="modal-overlay">
              <section
                  className="form-modal"
                  role="dialog"
                  aria-modal="true"
                  aria-labelledby="form-title"
              >
                <div className="modal-heading">
                  <h2 id="form-title">
                    {editingService
                        ? 'Edit Laundry Service'
                        : 'Add Laundry Service'}
                  </h2>

                  <button
                      type="button"
                      className="close-button"
                      onClick={closeForm}
                      aria-label="Close form"
                      disabled={saving}
                  >
                    ×
                  </button>
                </div>

                <form onSubmit={handleSubmit}>
                  <label htmlFor="service-name">Service Name</label>
                  <input
                      id="service-name"
                      type="text"
                      placeholder="e.g. Wash and Fold"
                      value={name}
                      onChange={(event) => setName(event.target.value)}
                      required
                  />

                  <label htmlFor="service-description">Description</label>
                  <textarea
                      id="service-description"
                      placeholder="Describe the service"
                      value={description}
                      onChange={(event) => setDescription(event.target.value)}
                      rows={3}
                  />

                  <label htmlFor="service-price">Price (₹)</label>
                  <input
                      id="service-price"
                      type="number"
                      placeholder="e.g. 100"
                      min="0.01"
                      step="0.01"
                      value={price}
                      onChange={(event) => setPrice(event.target.value)}
                      required
                  />

                  {formError && (
                      <p className="form-error" role="alert">
                        {formError}
                      </p>
                  )}

                  <div className="form-actions">
                    <button
                        type="button"
                        className="secondary-button"
                        onClick={closeForm}
                        disabled={saving}
                    >
                      Cancel
                    </button>

                    <button
                        type="submit"
                        className="primary-button"
                        disabled={saving}
                    >
                      {saving
                          ? 'Saving...'
                          : editingService
                              ? 'Update Service'
                              : 'Save Service'}
                    </button>
                  </div>
                </form>
              </section>
            </div>
        )}

        {showCustomerForm && (
            <div className="modal-overlay" onMouseDown={(event) => {
              if (event.target === event.currentTarget) closeCustomerForm()
            }}>
              <section
                  className="form-modal"
                  role="dialog"
                  aria-modal="true"
                  aria-labelledby="customer-form-title"
              >
                <div className="modal-heading">
                  <h2 id="customer-form-title">
                    {editingCustomer ? 'Edit Customer' : 'Add Customer'}
                  </h2>
                  <button
                      type="button"
                      className="close-button"
                      onClick={closeCustomerForm}
                      aria-label="Close customer form"
                      disabled={customerSaving}
                  >
                    ×
                  </button>
                </div>

                <form onSubmit={handleCustomerSubmit}>
                  <label htmlFor="customer-full-name">Full Name</label>
                  <input
                      id="customer-full-name"
                      type="text"
                      placeholder="e.g. Arun Kumar"
                      value={customerFullName}
                      onChange={(event) => setCustomerFullName(event.target.value)}
                      maxLength={100}
                      required
                  />

                  <label htmlFor="customer-phone">Phone Number</label>
                  <input
                      id="customer-phone"
                      type="tel"
                      placeholder="e.g. 9876543210"
                      value={customerPhoneNumber}
                      onChange={(event) => setCustomerPhoneNumber(event.target.value)}
                      maxLength={20}
                      required
                  />

                  <label htmlFor="customer-email">Email Address</label>
                  <input
                      id="customer-email"
                      type="email"
                      placeholder="e.g. arun@example.com"
                      value={customerEmail}
                      onChange={(event) => setCustomerEmail(event.target.value)}
                      maxLength={150}
                      required
                  />

                  {customerFormError && (
                      <p className="form-error" role="alert">{customerFormError}</p>
                  )}

                  <div className="form-actions">
                    <button
                        type="button"
                        className="secondary-button"
                        onClick={closeCustomerForm}
                        disabled={customerSaving}
                    >
                      Cancel
                    </button>
                    <button type="submit" className="primary-button" disabled={customerSaving}>
                      {customerSaving
                          ? 'Saving...'
                          : editingCustomer
                              ? 'Update Customer'
                              : 'Save Customer'}
                    </button>
                  </div>
                </form>
              </section>
            </div>
        )}
      </div>
  )
}

export default App
