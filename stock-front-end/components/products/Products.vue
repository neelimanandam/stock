<template>
  <v-container fluid ma-0 pa-0>
    <v-snackbar bottom color="primary" v-model="snackbar">{{ snackbarMessage }}</v-snackbar>
    <v-layout>
      <v-flex xs12 sm12 md6 offset-md3 la4 offset-la4 xl4 offset-xl4>
        <v-card>
          <v-layout pt-3>
            <product-form
              :dialog="showDialog"
              @dialog-closed="showDialog=false"
              :dialogHeader="dialogHeader"
              :product="selectedProduct"
              :submitLabel="submitLabel"
              :submitFunction="submitFunction"
            />
            <v-flex hidden-xs-only>
              <v-btn fab color="primary" @click="openAddProductDialog()" small>
                <v-icon>add</v-icon>
              </v-btn>
            </v-flex>
            <v-flex hidden-sm-and-up>
              <v-btn
                fab
                color="primary"
                @click="openAddProductDialog()"
                bottom
                right
                fixed
                icon
                small
              >
                <v-icon>add</v-icon>
              </v-btn>
            </v-flex>

            <v-layout>
              <v-card-title class="title">{{ $t('app.actions.products') }}</v-card-title>
            </v-layout>
          </v-layout>
          <v-card-text>
            <v-layout justify-center>
              <span v-if="loading">
                <v-progress-circular indeterminate color="secondary"></v-progress-circular>
                <span>Please wait..</span>
              </span>
              <v-list>
                <v-list-tile v-for="product in products" :key="product.id">
                  <v-list-tile-content>
                    <v-list-tile-title>
                      {{ product.name }}
                      <span class="caption">{{ product.category }}</span>
                    </v-list-tile-title>
                  </v-list-tile-content>

                  <v-list-tile-avatar>
                    <v-menu bottom left>
                      <template v-slot:activator="{ on }">
                        <v-icon color="primary" v-on="on">more_vert</v-icon>
                      </template>
                      <v-list>
                        <v-list-tile key="edit">
                          <v-list-tile-avatar>
                            <v-icon color="primary" @click="openEditProductDialog(product)">edit</v-icon>
                          </v-list-tile-avatar>
                        </v-list-tile>
                        <v-list-tile key="delete">
                          <v-list-tile-avatar>
                            <v-icon
                              color="primary"
                              @click="openDeleteProductWarningDialog(product)"
                              v-on="on"
                            >delete</v-icon>
                          </v-list-tile-avatar>
                        </v-list-tile>
                      </v-list>
                    </v-menu>
                  </v-list-tile-avatar>
                </v-list-tile>
              </v-list>
            </v-layout>
          </v-card-text>
        </v-card>
      </v-flex>
    </v-layout>
    <v-dialog v-model="deleteWarningDialog" persistent max-width="400">
      <v-card>
        <v-card-title>Are you sure you want to get rid of this?</v-card-title>
        <v-card-actions>
          <v-btn color="primary" @click="deleteWarningDialog=false">Cancel</v-btn>
          <v-btn @click="deleteSelectedProduct()">Sure</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>
<script>
import ProductForm from './ProductForm'
import { mapGetters, mapActions } from 'vuex'
export default {
  components: {
    ProductForm
  },
  data() {
    return {
      loading: true,
      dialog: false,
      valid: false,
      snackbar: false,
      on: false,
      showDialog: false,
      snackbarMessage: null,
      selectedProduct: null,
      deleteWarningDialog: false,
      dialogHeader: null,
      submitLabel: null,
      submitFunction: null
    }
  },
  computed: {
    ...mapGetters({
      products: 'products/products'
    })
  },
  methods: {
    ...mapActions({
      getProducts: 'products/getProducts',
      createProduct: 'products/createProduct',
      updateProduct: 'products/updateProduct',
      removeProduct: 'products/removeProduct'
    }),
    addNewProduct: function(payload) {
      this.createProduct(payload)
        .then(response => {
          if (response.data) {
            this.snackbarMessage =
              'New product ' + payload.name + ' added successfully!'
            this.snackbar = true
            this.showDialog = false
          }
        })
        .catch(error => {
          console.error(error)
        })
    },
    openDeleteProductWarningDialog: function(product) {
      this.selectedProduct = product
      this.deleteWarningDialog = true
    },
    deleteSelectedProduct: function() {
      this.removeProduct(this.selectedProduct.id)
        .then(response => {
          if (response.data) {
            this.snackbarMessage =
              'Product ' + this.selectedProduct.name + ' deleted successfully!'
            this.snackbar = true
            this.deleteWarningDialog = false
          }
        })
        .catch(error => {
          console.error(error)
        })
    },
    updateSelectedProduct: function(payload) {
      this.updateProduct(payload)
        .then(response => {
          if (response.data) {
            this.snackbarMessage =
              'Product ' + payload.name + ' updated successfully!'
            this.snackbar = true
            this.showDialog = false
          }
        })
        .catch(error => {
          console.error(error)
        })
    },
    openAddProductDialog: function() {
      this.showDialog = true
      this.dialogHeader = 'Add new Product'
      this.selectedProduct = null
      this.submitLabel = 'Save'
      this.submitFunction = this.addNewProduct
    },
    openEditProductDialog: function(product) {
      this.showDialog = true
      this.dialogHeader = 'Edit Product'
      this.selectedProduct = product
      this.submitLabel = 'Update'
      this.submitFunction = this.updateSelectedProduct
    }
  },
  mounted: function() {
    this.getProducts().then(response => {
      this.loading = false
    })
  }
}
</script>
