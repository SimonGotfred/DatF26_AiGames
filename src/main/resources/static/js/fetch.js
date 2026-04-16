
// "global" shorthand methods for communication with backend
const getBackend    = async function(endpoint, authorization)
                    { return await wireBackend("GET",   endpoint, authorization ); }
const putBackend    = async function(endpoint, payload, authorization)
                    { return await wireBackend("PUT",   endpoint, authorization, payload, ); }
const postBackend   = async function(endpoint, payload, authorization)
                    { return await wireBackend("POST",  endpoint, authorization, payload, ); }
const patchBackend  = async function(endpoint, payload, authorization)
                    { return await wireBackend("PATCH", endpoint, authorization, payload, ); }
const deleteBackend = async function(endpoint, payload, authorization)
                    { return await wireBackend("DELETE",endpoint, authorization, payload, ); }

const wireBackend = async function(method, endpoint, authorization, payload)
{
    const fetchOptions = {headers:new Headers(),method:method};

    if (authorization)
    {
        fetchOptions.headers.set('Authorization', authorization);
    }
    if (payload)
    {
        fetchOptions.headers.set('content-type',"application/json");
        fetchOptions.body = JSON.stringify(payload);
    }

    const url = window.location.origin + '/' + endpoint;
    return await fetch(url, fetchOptions);
}

// export {getBackend,putBackend,postBackend,patchBackend,deleteBackend,wireBackend};